package com.attendance.service;

import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.PageResponse;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.FaceMatchResponse;
import com.attendance.entity.AttendanceLog;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.AttendanceLogRepository;
import com.attendance.repository.spec.AttendanceSpecifications;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.exception.AppException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeService employeeService;
    private final AttendanceMapper attendanceMapper;

    @Value("${app.attendance.min-checkout-after-minutes:30}")
    private long minCheckoutAfterMinutes;

    // Check-in nhân viên theo ID và xác thực nhân viên tồn tại trong HR.
    @Transactional
    public AttendanceResponse checkIn(Long employeeId) {
        requireEmployee(employeeId);
        return performCheckIn(employeeId);
    }

    @Transactional
    public AttendanceResponse scanByFace(FaceDescriptorRequest request) {
        Long employeeId = matchEmployeeIdByFace(request);
        HrEmployeeSnapshot hr = requireEmployee(employeeId);
        Attendance existing = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now()).orElse(null);

        return withEmployeeBrief(resolveScanResult(employeeId, existing), hr);
    }

    private AttendanceResponse resolveScanResult(Long employeeId, Attendance existing) {
        if (existing == null) {
            return performCheckIn(employeeId);
        }

        if (existing.getCheckInTime() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Hôm nay không có lượt check-in hợp lệ để check-out");
        }

        if (existing.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã hoàn tất chấm công hôm nay");
        }

        if (isCheckoutTooSoon(existing, LocalDateTime.now())) {
            return attendanceMapper.toResponse(existing);
        }

        return performCheckOut(employeeId);
    }

    private Long matchEmployeeIdByFace(FaceDescriptorRequest request) {
        FaceMatchResponse match;
        try {
            match = employeeService.matchFace(request);
        } catch (Exception e) {
            log.error("Lỗi khi so khớp khuôn mặt: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_INPUT, "Không nhận diện được nhân viên");
        }
        if (match == null || match.employeeId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không nhận diện được nhân viên");
        }
        return match.employeeId();
    }

    // Ghép thêm thông tin nhân viên từ HR vào response chấm công.
    private static AttendanceResponse withEmployeeBrief(AttendanceResponse base, HrEmployeeSnapshot hr) {
        if (hr == null) {
            return base;
        }
        return new AttendanceResponse(
                base.id(),
                base.employeeId(),
                base.checkInTime(),
                base.checkOutTime(),
                base.workDate(),
                base.status(),
                base.lateMinutes(),
                base.earlyLeaveMinutes(),
                base.workedMinutes(),
                base.expectedMinutes(),
                base.createdAt(),
                hr.fullName(),
                hr.employeeCode(),
                hr.departmentName(),
                hr.positionName());
    }

    // Tạo bản ghi check-in cho ngày hiện tại và xác định trạng thái đi làm.
    private AttendanceResponse performCheckIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).isPresent()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-in hôm nay rồi");
        }

        Shift shift = getTodayShift(employeeId, today);
        String status = "PRESENT";
        int lateMinutes = 0;
        int expectedMinutes = shift == null ? 0 : calculateExpectedMinutes(shift);

        if (shift != null) {
            LocalTime allowedLateTime = shift.getStartTime().plusMinutes(shift.getGracePeriod() != null ? shift.getGracePeriod() : 0);
            if (nowTime.isAfter(allowedLateTime)) {
                status = "LATE";
                lateMinutes = Math.toIntExact(Duration.between(shift.getStartTime(), nowTime).toMinutes());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .workDate(today)
                .checkInTime(now)
                .status(status)
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(0)
                .workedMinutes(0)
                .expectedMinutes(expectedMinutes)
                .build();
        try {
            Attendance saved = attendanceRepository.save(attendance);
            recordLog(employeeId, now, "IN");
            return attendanceMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-in hôm nay rồi");
        }
    }

    // Check-out nhân viên trong ngày hiện tại.
    @Transactional
    public AttendanceResponse checkOut(Long employeeId) {
        requireEmployee(employeeId);
        return performCheckOut(employeeId);
    }

    private AttendanceResponse performCheckOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Bạn chưa check-in hôm nay"));

        if (attendance.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-out hôm nay rồi");
        }

        LocalDateTime now = LocalDateTime.now();
        if (isCheckoutTooSoon(attendance, now)) {
            throw new AppException(
                    ErrorCode.INVALID_INPUT,
                    "Bạn vừa check-in, vui lòng chờ ít nhất " + minCheckoutAfterMinutes + " phút trước khi check-out");
        }

        attendance.setCheckOutTime(now);

        Shift shift = getTodayShift(employeeId, today);
        int earlyLeaveMinutes = 0;
        if (shift != null && nowTime.isBefore(shift.getEndTime())) {
            earlyLeaveMinutes = Math.toIntExact(Duration.between(nowTime, shift.getEndTime()).toMinutes());
            if ("PRESENT".equals(attendance.getStatus())) {
                attendance.setStatus("EARLY_LEAVE");
            } else if ("LATE".equals(attendance.getStatus())) {
                attendance.setStatus("LATE_AND_EARLY_LEAVE");
            }
        }
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);
        attendance.setWorkedMinutes(calculateWorkedMinutes(attendance.getCheckInTime(), now, shift));
        attendance.setExpectedMinutes(shift == null ? 0 : calculateExpectedMinutes(shift));

        Attendance saved = attendanceRepository.save(attendance);
        recordLog(employeeId, now, "OUT");
        return attendanceMapper.toResponse(saved);
    }

    // Lấy bản ghi chấm công của nhân viên trong ngày hôm nay.
    public AttendanceResponse getTodayByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(attendanceMapper::toResponse)
                .orElse(null);
    }

    // Tìm kiếm chấm công theo bộ lọc và phân trang.
    public PageResponse<AttendanceResponse> search(
            Long employeeId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            Pageable pageable) {
        Page<Attendance> page = attendanceRepository.findAll(
                AttendanceSpecifications.matches(employeeId, date, fromDate, toDate, status), pageable);
        return PageResponse.of(page.map(attendanceMapper::toResponse));
    }

    @Transactional
    public void delete(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_NOT_FOUND));
        attendanceRepository.delete(attendance);
    }

    // Kiểm tra nhân viên có tồn tại trong HR và lấy thông tin tóm tắt.
    private HrEmployeeSnapshot requireEmployee(Long employeeId) {
        HrEmployeeSnapshot result = employeeService.getEmployeeSnapshot(employeeId);
        if (result == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Nhân viên không tồn tại trong hệ thống HR");
        }
        return result;
    }

    // Lấy ca làm trong ngày hiện tại của nhân viên.
    private Shift getTodayShift(Long employeeId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return employeeScheduleRepository.findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqual(employeeId, date)
                .stream()
                .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                .map(EmployeeSchedule::getShift)
                .findFirst()
                .orElse(null);
    }

    private void recordLog(Long employeeId, LocalDateTime logTime, String logType) {
        AttendanceLog log = AttendanceLog.builder()
                .employeeId(employeeId)
                .logTime(logTime)
                .logType(logType)
                .deviceId("WEB")
                .build();
        attendanceLogRepository.save(log);
    }

    private boolean isCheckoutTooSoon(Attendance attendance, LocalDateTime now) {
        if (minCheckoutAfterMinutes <= 0 || attendance.getCheckInTime() == null) {
            return false;
        }
        return now.isBefore(attendance.getCheckInTime().plusMinutes(minCheckoutAfterMinutes));
    }

    private int calculateExpectedMinutes(Shift shift) {
        int total = minutesBetween(shift.getStartTime(), shift.getEndTime());
        int breakMinutes = 0;
        if (shift.getBreakStart() != null && shift.getBreakEnd() != null) {
            breakMinutes = minutesBetween(shift.getBreakStart(), shift.getBreakEnd());
        }
        return Math.max(0, total - breakMinutes);
    }

    private int calculateWorkedMinutes(LocalDateTime checkIn, LocalDateTime checkOut, Shift shift) {
        long total = Duration.between(checkIn, checkOut).toMinutes();
        if (shift != null && shift.getBreakStart() != null && shift.getBreakEnd() != null) {
            int breakMinutes = minutesBetween(shift.getBreakStart(), shift.getBreakEnd());
            total -= breakMinutes;
        }
        return Math.toIntExact(Math.max(0, total));
    }

    private int minutesBetween(LocalTime start, LocalTime end) {
        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();
        if (endMinutes < startMinutes) {
            endMinutes += 24 * 60;
        }
        return endMinutes - startMinutes;
    }
}
