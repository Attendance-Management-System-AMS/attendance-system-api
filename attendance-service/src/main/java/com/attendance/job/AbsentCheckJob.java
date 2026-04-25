package com.attendance.job;

import com.attendance.client.RequestClient;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsentCheckJob {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final RequestClient requestClient;

    // Đánh dấu những nhân viên không có bản ghi chấm công thành vắng mặt vào cuối ngày.
    @Scheduled(cron = "0 55 23 * * ?", zone = "Asia/Bangkok") // Chạy vào lúc 23:55 mỗi ngày
    public void markAbsentEmployees() {
        log.info("Bắt đầu chạy Cron Job đánh dấu nhân viên vắng mặt...");
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        List<EmployeeSchedule> activeSchedulesToday = resolveEffectiveSchedules(dayOfWeek, today);

        boolean holiday = holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(today, today);
        int absentCount = 0;
        int onLeaveCount = 0;
        int holidayCount = 0;
        int missingCheckoutCount = 0;
        for (EmployeeSchedule schedule : activeSchedulesToday) {
            Long employeeId = schedule.getEmployeeId();
            Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);

            if (existingAttendance.isPresent()) {
                Attendance attendance = existingAttendance.get();
                if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null) {
                    attendance.setStatus("MISSING_CHECKOUT");
                    attendanceRepository.save(attendance);
                    missingCheckoutCount++;
                }
                continue;
            }

            String status = "ABSENT";
            if (holiday) {
                status = "HOLIDAY";
                holidayCount++;
            } else if (requestClient.hasApprovedLeave(employeeId, today)) {
                status = "ON_LEAVE";
                onLeaveCount++;
            } else {
                absentCount++;
            }

            Attendance attendanceRecord = Attendance.builder()
                    .employeeId(employeeId)
                    .workDate(today)
                    .status(status)
                    .build();
            try {
                attendanceRepository.save(attendanceRecord);
            } catch (DataIntegrityViolationException ex) {
                log.debug("Bỏ qua do bản ghi chấm công đã tồn tại: employeeId={}, workDate={}", employeeId, today);
            }
        }
        log.info("Hoàn tất Job. ABSENT={}, ON_LEAVE={}, HOLIDAY={}, MISSING_CHECKOUT={}",
                absentCount, onLeaveCount, holidayCount, missingCheckoutCount);
    }

    private List<EmployeeSchedule> resolveEffectiveSchedules(int dayOfWeek, LocalDate workDate) {
        Map<Long, EmployeeSchedule> latestByEmployee = new LinkedHashMap<>();

        employeeScheduleRepository.findByEffectiveFromLessThanEqual(workDate).stream()
                .filter(schedule -> Boolean.TRUE.equals(schedule.getIsActive()))
                .filter(schedule -> schedule.getEffectiveTo() == null || !schedule.getEffectiveTo().isBefore(workDate))
                .filter(schedule -> dayOfWeek == schedule.getDayOfWeek())
                .forEach(schedule -> {
                    EmployeeSchedule current = latestByEmployee.get(schedule.getEmployeeId());
                    if (current == null || schedule.getEffectiveFrom().isAfter(current.getEffectiveFrom())) {
                        latestByEmployee.put(schedule.getEmployeeId(), schedule);
                    }
                });

        return new ArrayList<>(latestByEmployee.values());
    }
}


