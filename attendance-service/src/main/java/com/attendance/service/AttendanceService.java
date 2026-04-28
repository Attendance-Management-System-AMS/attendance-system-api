package com.attendance.service;

import com.attendance.client.HrClient;
import com.attendance.client.RequestClient;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.AttendanceResponse;
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
import com.attendance.repository.HolidayRepository;
import com.attendance.repository.spec.AttendanceSpecifications;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.exception.AppException;
import com.attendance.util.ShiftUtils;
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
    private final HrClient hrClient;
    private final AttendanceMapper attendanceMapper;
    private final HolidayRepository holidayRepository;
    private final RequestClient requestClient;

    @Value("${app.attendance.min-checkout-after-minutes:30}")
    private long minCheckoutAfterMinutes;

    // Check-in nhân viên theo ID và xác thực nhân viên tồn tại trong HR.
    @Transactional
    public AttendanceResponse checkIn(Long employeeId) {
        requireEmployee(employeeId);
        return performCheckIn(employeeId, "WEB");
    }

    @Transactional
    public AttendanceResponse scanByFace(FaceDescriptorRequest request) {
        return scanByFace(request, "KIOSK");
    }

    @Transactional
    public AttendanceResponse scanByFace(FaceDescriptorRequest request, String deviceId) {
        Long employeeId = matchEmployeeIdByFace(request);
        HrEmployeeSnapshot hr = requireEmployee(employeeId);
        Attendance existing = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now()).orElse(null);

        return withEmployeeBrief(resolveScanResult(employeeId, existing, deviceId), hr);
    }

    private AttendanceResponse resolveScanResult(Long employeeId, Attendance existing, String deviceId) {
        if (existing == null) {
            return performCheckIn(employeeId, deviceId);
        }

        if ("ON_LEAVE".equalsIgnoreCase(existing.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đang có đơn nghỉ đã được duyệt cho hôm nay");
        }

        if (existing.getCheckInTime() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Hôm nay không có lượt check-in hợp lệ để check-out");
        }

        if (existing.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã hoàn tất chấm công hôm nay");
        }

        if (isCheckoutTooSoon(existing, LocalDateTime.now())) {
            throw new AppException(
                    ErrorCode.INVALID_INPUT,
                    "Bạn vừa check-in, vui lòng chờ ít nhất " + minCheckoutAfterMinutes + " phút trước khi check-out");
        }

        return performCheckOut(employeeId, deviceId);
    }

    private Long matchEmployeeIdByFace(FaceDescriptorRequest request) {
        FaceMatchResponse match;
        try {
            match = hrClient.matchFace(request);
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
    private AttendanceResponse performCheckIn(Long employeeId, String deviceId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        Attendance existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).orElse(null);
        if (existingAttendance != null) {
            if ("ON_LEAVE".equalsIgnoreCase(existingAttendance.getStatus())) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đang có đơn nghỉ đã được duyệt cho hôm nay");
            }
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-in hôm nay rồi");
        }
        if (hasApprovedLeaveSafely(employeeId, today)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đang có đơn nghỉ đã được duyệt cho hôm nay");
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
            recordLog(employeeId, now, "IN", deviceId);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-in hôm nay rồi");
        }
    }

    // Check-out nhân viên trong ngày hiện tại.
    @Transactional
    public AttendanceResponse checkOut(Long employeeId) {
        requireEmployee(employeeId);
        return performCheckOut(employeeId, "WEB");
    }

    private AttendanceResponse performCheckOut(Long employeeId, String deviceId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findFirstByEmployeeIdAndCheckOutTimeIsNullAndWorkDateBetweenOrderByWorkDateDesc(
                        employeeId,
                        today.minusDays(1),
                        today)
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

        Shift shift = getTodayShift(employeeId, attendance.getWorkDate());
        String baseStatus = resolveCheckoutBaseStatus(attendance);
        int earlyLeaveMinutes = 0;
        LocalDateTime scheduledEnd = shift == null ? null : ShiftUtils.resolveShiftEnd(attendance.getWorkDate(), shift);
        if (scheduledEnd != null && now.isBefore(scheduledEnd)) {
            earlyLeaveMinutes = Math.toIntExact(Duration.between(now, scheduledEnd).toMinutes());
            if ("PRESENT".equals(baseStatus)) {
                attendance.setStatus("EARLY_LEAVE");
            } else if ("LATE".equals(baseStatus)) {
                attendance.setStatus("LATE_AND_EARLY_LEAVE");
            }
        } else {
            attendance.setStatus(baseStatus);
        }
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);
        attendance.setWorkedMinutes(calculateWorkedMinutes(attendance.getCheckInTime(), now, shift));
        attendance.setExpectedMinutes(shift == null ? 0 : calculateExpectedMinutes(shift));

        Attendance saved = attendanceRepository.save(attendance);
        recordLog(employeeId, now, "OUT", deviceId);
        return toResponse(saved);
    }

    // Lấy bản ghi chấm công của nhân viên trong ngày hôm nay.
    public AttendanceResponse getTodayByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(this::toResponse)
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
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public void delete(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_NOT_FOUND));
        attendanceRepository.delete(attendance);
    }

    @Transactional
    public void syncApprovedLeave(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        if (employeeId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên là bắt buộc");
        }
        if (fromDate == null || toDate == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Khoảng ngày nghỉ là bắt buộc");
        }
        if (toDate.isBefore(fromDate)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        LocalDate workDate = fromDate;
        while (!workDate.isAfter(toDate)) {
            syncApprovedLeaveForDate(employeeId, workDate);
            workDate = workDate.plusDays(1);
        }
    }

    // Kiểm tra nhân viên có tồn tại trong HR và lấy thông tin tóm tắt.
    private HrEmployeeSnapshot requireEmployee(Long employeeId) {
        HrEmployeeSnapshot result = hrClient.getEmployeeSnapshot(employeeId);
        if (result == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Nhân viên không tồn tại trong hệ thống HR");
        }
        return result;
    }

    private void syncApprovedLeaveForDate(Long employeeId, LocalDate workDate) {
        if (holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(workDate, workDate)) {
            return;
        }

        Attendance existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, workDate).orElse(null);
        if (existingAttendance == null) {
            if (getTodayShift(employeeId, workDate) == null) {
                return;
            }
            attendanceRepository.save(buildApprovedLeaveAttendance(employeeId, workDate));
            return;
        }

        if (hasRecordedTime(existingAttendance) || "HOLIDAY".equalsIgnoreCase(existingAttendance.getStatus())) {
            return;
        }

        if ("ON_LEAVE".equalsIgnoreCase(existingAttendance.getStatus())) {
            return;
        }

        existingAttendance.setCheckInTime(null);
        existingAttendance.setCheckOutTime(null);
        existingAttendance.setStatus("ON_LEAVE");
        existingAttendance.setLateMinutes(0);
        existingAttendance.setEarlyLeaveMinutes(0);
        existingAttendance.setWorkedMinutes(0);
        existingAttendance.setExpectedMinutes(0);
        attendanceRepository.save(existingAttendance);
    }

    // Lấy ca làm trong ngày hiện tại của nhân viên.
    private Shift getTodayShift(Long employeeId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return employeeScheduleRepository.findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employeeId, date)
                .stream()
                .filter(schedule -> schedule.getEffectiveTo() == null || !schedule.getEffectiveTo().isBefore(date))
                .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                .map(EmployeeSchedule::getShift)
                .findFirst()
                .orElse(null);
    }

    private Attendance buildApprovedLeaveAttendance(Long employeeId, LocalDate workDate) {
        return Attendance.builder()
                .employeeId(employeeId)
                .workDate(workDate)
                .status("ON_LEAVE")
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .workedMinutes(0)
                .expectedMinutes(0)
                .build();
    }

    private void recordLog(Long employeeId, LocalDateTime logTime, String logType, String deviceId) {
        AttendanceLog log = AttendanceLog.builder()
                .employeeId(employeeId)
                .logTime(logTime)
                .logType(logType)
                .deviceId(deviceId == null || deviceId.isBlank() ? "WEB" : deviceId.trim())
                .build();
        attendanceLogRepository.save(log);
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceResponse response = attendanceMapper.toResponse(attendance);
        if (isPastMissingCheckout(attendance)) {
            return response.withStatus("MISSING_CHECKOUT");
        }
        return response;
    }

    private boolean isPastMissingCheckout(Attendance attendance) {
        return attendance.getWorkDate() != null
                && attendance.getWorkDate().isBefore(LocalDate.now())
                && attendance.getCheckInTime() != null
                && attendance.getCheckOutTime() == null;
    }

    private boolean hasRecordedTime(Attendance attendance) {
        return attendance.getCheckInTime() != null || attendance.getCheckOutTime() != null;
    }

    private boolean hasApprovedLeaveSafely(Long employeeId, LocalDate date) {
        try {
            return requestClient.hasApprovedLeave(employeeId, date);
        } catch (Exception ex) {
            log.warn("Không kiểm tra được đơn nghỉ đã duyệt cho employeeId={}, date={}: {}", employeeId, date, ex.getMessage());
            return false;
        }
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
            total -= calculateBreakOverlapMinutes(checkIn, checkOut, shift);
        }
        return Math.toIntExact(Math.max(0, total));
    }

    private int calculateBreakOverlapMinutes(LocalDateTime checkIn, LocalDateTime checkOut, Shift shift) {
        LocalDate workDate = checkIn.toLocalDate();
        LocalDateTime breakStart = workDate.atTime(shift.getBreakStart());
        LocalDateTime breakEnd = workDate.atTime(shift.getBreakEnd());
        if (shift.getBreakEnd().isBefore(shift.getBreakStart())) {
            breakEnd = breakEnd.plusDays(1);
        }
        if (shift.getBreakStart().isBefore(shift.getStartTime())) {
            breakStart = breakStart.plusDays(1);
            breakEnd = breakEnd.plusDays(1);
        }

        LocalDateTime overlapStart = checkIn.isAfter(breakStart) ? checkIn : breakStart;
        LocalDateTime overlapEnd = checkOut.isBefore(breakEnd) ? checkOut : breakEnd;
        if (!overlapEnd.isAfter(overlapStart)) {
            return 0;
        }
        return Math.toIntExact(Duration.between(overlapStart, overlapEnd).toMinutes());
    }

    private int minutesBetween(LocalTime start, LocalTime end) {
        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();
        if (endMinutes < startMinutes) {
            endMinutes += 24 * 60;
        }
        return endMinutes - startMinutes;
    }

    private String resolveCheckoutBaseStatus(Attendance attendance) {
        if (attendance.getLateMinutes() != null && attendance.getLateMinutes() > 0) {
            return "LATE";
        }
        if ("LATE".equalsIgnoreCase(attendance.getStatus())
                || "LATE_AND_EARLY_LEAVE".equalsIgnoreCase(attendance.getStatus())) {
            return "LATE";
        }
        if ("MISSING_CHECKOUT".equalsIgnoreCase(attendance.getStatus())
                || "EARLY_LEAVE".equalsIgnoreCase(attendance.getStatus())
                || attendance.getStatus() == null
                || attendance.getStatus().isBlank()) {
            return "PRESENT";
        }
        return attendance.getStatus();
    }
}
