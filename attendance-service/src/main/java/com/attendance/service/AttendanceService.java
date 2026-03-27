package com.attendance.service;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.feign.HrClient;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.AttendanceSpecifications;
import com.attendance.repository.EmployeeScheduleRepository;
import com.common.exception.AppException;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final HrClient hrClient;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceResponse checkIn(Long employeeId) {
        validateEmployee(employeeId);

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).isPresent()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Bạn đã check-in hôm nay rồi");
        }

        Shift shift = getTodayShift(employeeId, today);
        String status = "PRESENT";

        if (shift != null) {
            LocalTime allowedLateTime = shift.getStartTime().plusMinutes(shift.getGracePeriod() != null ? shift.getGracePeriod() : 0);
            if (nowTime.isAfter(allowedLateTime)) {
                status = "LATE";
            }
        }

        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .workDate(today)
                .checkInTime(LocalDateTime.now())
                .status(status)
                .build();
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Bạn chưa check-in hôm nay"));

        attendance.setCheckOutTime(LocalDateTime.now());

        Shift shift = getTodayShift(employeeId, today);
        if (shift != null && nowTime.isBefore(shift.getEndTime())) {
            if ("PRESENT".equals(attendance.getStatus())) {
                attendance.setStatus("EARLY_LEAVE");
            } else if ("LATE".equals(attendance.getStatus())) {
                attendance.setStatus("LATE_AND_EARLY_LEAVE");
            }
        }

        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    public List<AttendanceResponse> getByEmployee(Long employeeId) {
        return attendanceRepository.findAll(
                        AttendanceSpecifications.matches(employeeId, null, null, null, null),
                        Sort.by(Sort.Direction.DESC, "workDate"))
                .stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public AttendanceResponse getTodayByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(attendanceMapper::toResponse)
                .orElse(null);
    }

    public List<AttendanceResponse> getAttendancesByDate(LocalDate date) {
        LocalDate workDate = (date != null) ? date : LocalDate.now();
        return attendanceRepository.findAll(
                        AttendanceSpecifications.matches(null, workDate, null, null, null),
                        Sort.by(Sort.Direction.ASC, "employeeId"))
                .stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public Page<AttendanceResponse> search(
            Long employeeId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            Pageable pageable) {
        return attendanceRepository
                .findAll(AttendanceSpecifications.matches(employeeId, date, fromDate, toDate, status), pageable)
                .map(attendanceMapper::toResponse);
    }

    private void validateEmployee(Long employeeId) {
        try {
            hrClient.getEmployeeById(employeeId);
        } catch (FeignException e) {
            log.error("Lỗi khi xác thực nhân viên: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_INPUT, "Nhân viên không tồn tại trong hệ thống HR");
        }
    }

    private Shift getTodayShift(Long employeeId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return employeeScheduleRepository.findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqual(employeeId, date)
                .stream()
                .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                .map(EmployeeSchedule::getShift)
                .findFirst()
                .orElse(null);
    }
}
