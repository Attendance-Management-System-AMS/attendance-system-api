package com.attendance.job;

import com.attendance.client.HrClient;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.HolidayRepository;
import com.attendance.util.ShiftUtils;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsentCheckJob {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final HrClient hrClient;
    private final Clock clock;
    @Autowired(required = false)
    private com.attendance.service.OvertimeRequestService overtimeRequestService;

    // Chạy định kỳ để hoàn tất ca làm đã qua giờ kết thúc, kể cả ca xuyên đêm từ hôm trước.
    @Scheduled(cron = "0 */15 * * * ?", zone = "Asia/Bangkok")
    public void markAbsentEmployees() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        JobStats stats = new JobStats();

        log.info("Bắt đầu chạy Cron Job hoàn tất bảng công tại {}", now);
        stats.add(processWorkDate(today.minusDays(1), now));
        stats.add(processWorkDate(today, now));

        log.info("Hoàn tất Job. ABSENT={}, ON_LEAVE={}, HOLIDAY={}, MISSING_CHECKOUT={}",
                stats.absentCount, stats.onLeaveCount, stats.holidayCount, stats.missingCheckoutCount);
    }

    private JobStats processWorkDate(LocalDate workDate, LocalDateTime now) {
        int dayOfWeek = workDate.getDayOfWeek().getValue();
        List<EmployeeSchedule> activeSchedules = resolveEffectiveSchedules(dayOfWeek, workDate);
        boolean holiday = holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(workDate, workDate);
        JobStats stats = new JobStats();

        for (EmployeeSchedule schedule : activeSchedules) {
            Long employeeId = schedule.getEmployeeId();
            Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, workDate);
            boolean shiftEnded = hasShiftEnded(schedule.getShift(), workDate, now);

            if (existingAttendance.isPresent()) {
                Attendance attendance = existingAttendance.get();
                if (shiftEnded && attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null) {
                    attendance.setStatus("MISSING_CHECKOUT");
                    attendanceRepository.save(attendance);
                    recalculateOvertimeIfAvailable(employeeId, workDate);
                    stats.missingCheckoutCount++;
                }
                continue;
            }

            String status = null;
            if (holiday) {
                status = "HOLIDAY";
                stats.holidayCount++;
            } else {
                Boolean hasApprovedLeave = hasApprovedLeaveSafely(employeeId, workDate);
                if (hasApprovedLeave == null) {
                    continue;
                }
                if (hasApprovedLeave) {
                    status = "ON_LEAVE";
                    stats.onLeaveCount++;
                } else if (shiftEnded) {
                    status = "ABSENT";
                    stats.absentCount++;
                }
            }

            if (status == null) {
                continue;
            }

            Attendance attendanceRecord = Attendance.builder()
                    .employeeId(employeeId)
                    .workDate(workDate)
                    .status(status)
                    .build();
            try {
                attendanceRepository.save(attendanceRecord);
                recalculateOvertimeIfAvailable(employeeId, workDate);
            } catch (DataIntegrityViolationException ex) {
                log.debug("Bỏ qua do bản ghi chấm công đã tồn tại: employeeId={}, workDate={}", employeeId, workDate);
            }
        }

        return stats;
    }

    private List<EmployeeSchedule> resolveEffectiveSchedules(int dayOfWeek, LocalDate workDate) {
        Map<Long, EmployeeSchedule> latestByEmployee = new LinkedHashMap<>();
        Set<Long> ambiguousEmployees = new HashSet<>();

        employeeScheduleRepository.findByEffectiveFromLessThanEqual(workDate).stream()
                .filter(schedule -> Boolean.TRUE.equals(schedule.getIsActive()))
                .filter(schedule -> schedule.getEffectiveTo() == null || !schedule.getEffectiveTo().isBefore(workDate))
                .filter(schedule -> dayOfWeek == schedule.getDayOfWeek())
                .forEach(schedule -> {
                    EmployeeSchedule current = latestByEmployee.get(schedule.getEmployeeId());
                    if (current == null || schedule.getEffectiveFrom().isAfter(current.getEffectiveFrom())) {
                        latestByEmployee.put(schedule.getEmployeeId(), schedule);
                        ambiguousEmployees.remove(schedule.getEmployeeId());
                    } else if (sameEffectiveFrom(current.getEffectiveFrom(), schedule.getEffectiveFrom())) {
                        ambiguousEmployees.add(schedule.getEmployeeId());
                    }
                });

        ambiguousEmployees.forEach(employeeId -> {
            latestByEmployee.remove(employeeId);
            log.warn("Bỏ qua employeeId={} vì có nhiều lịch cùng ngày hiệu lực, cần chuẩn hóa dữ liệu phân ca", employeeId);
        });

        return new ArrayList<>(latestByEmployee.values());
    }

    private boolean hasShiftEnded(Shift shift, LocalDate workDate, LocalDateTime now) {
        if (shift == null) {
            return !now.isBefore(workDate.atTime(23, 59, 59));
        }
        return !now.isBefore(ShiftUtils.resolveShiftEnd(workDate, shift));
    }

    private Boolean hasApprovedLeaveSafely(Long employeeId, LocalDate workDate) {
        try {
            return hrClient.hasApprovedLeave(employeeId, workDate);
        } catch (Exception ex) {
            log.warn(
                    "Bỏ qua employeeId={} cho workDate={} vì không kiểm tra được trạng thái nghỉ phép từ hr-service: {}",
                    employeeId,
                    workDate,
                    ex.getMessage());
            return null;
        }
    }

    private boolean sameEffectiveFrom(LocalDate first, LocalDate second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.isEqual(second);
    }

    private static final class JobStats {
        private int absentCount;
        private int onLeaveCount;
        private int holidayCount;
        private int missingCheckoutCount;

        private void add(JobStats other) {
            this.absentCount += other.absentCount;
            this.onLeaveCount += other.onLeaveCount;
            this.holidayCount += other.holidayCount;
            this.missingCheckoutCount += other.missingCheckoutCount;
        }
    }

    private void recalculateOvertimeIfAvailable(Long employeeId, LocalDate workDate) {
        if (overtimeRequestService != null) {
            overtimeRequestService.recalculateAttendanceOvertime(employeeId, workDate);
        }
    }
}


