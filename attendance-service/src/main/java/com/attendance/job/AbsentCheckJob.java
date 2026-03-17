package com.attendance.job;

import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsentCheckJob {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;

    @Scheduled(cron = "0 55 23 * * ?") // Chạy vào lúc 23:55 mỗi ngày
    public void markAbsentEmployees() {
        log.info("Bắt đầu chạy Cron Job đánh dấu nhân viên vắng mặt...");
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        List<EmployeeSchedule> activeSchedulesToday = employeeScheduleRepository
                .findByDayOfWeekAndIsActiveTrue(dayOfWeek)
                .stream()
                .filter(s -> !s.getEffectiveFrom().isAfter(today))
                .toList();

        int absentCount = 0;
        for (EmployeeSchedule schedule : activeSchedulesToday) {
            Long employeeId = schedule.getEmployeeId();
            boolean isPresent = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).isPresent();

            if (!isPresent) {
                // Không có bản ghi check-in nào trong ngày -> Vắng mặt
                Attendance absentRecord = Attendance.builder()
                        .employeeId(employeeId)
                        .workDate(today)
                        .status("ABSENT")
                        // checkInTime = null, checkOutTime = null
                        .build();
                attendanceRepository.save(absentRecord);
                absentCount++;
            }
        }
        log.info("Hoàn tất Job. Đã đánh dấu vắng mặt cho {} nhân viên.", absentCount);
    }
}
