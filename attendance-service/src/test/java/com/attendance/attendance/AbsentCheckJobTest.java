package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.client.HrClient;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.job.AbsentCheckJob;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.HolidayRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbsentCheckJobTest {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private HrClient hrClient;

    @Test
    void doesNotMarkOvernightAttendanceMissingBeforeShiftEnds() {
        LocalDate workDate = LocalDate.of(2026, 4, 25);
        Clock clock = fixedClock(LocalDateTime.of(2026, 4, 25, 23, 55));
        AbsentCheckJob job = new AbsentCheckJob(
                attendanceRepository,
                employeeScheduleRepository,
                holidayRepository,
                hrClient,
                clock);

        EmployeeSchedule schedule = createSchedule(4L, workDate.getDayOfWeek().getValue(), createShift("Ca đêm", 22, 0, 6, 0), workDate.minusDays(7));
        Attendance openAttendance = Attendance.builder()
                .id(1L)
                .employeeId(4L)
                .workDate(workDate)
                .status("PRESENT")
                .checkInTime(workDate.atTime(22, 5))
                .build();

        when(employeeScheduleRepository.findByEffectiveFromLessThanEqual(any(LocalDate.class))).thenReturn(List.of(schedule));
        when(holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(any(), any())).thenReturn(false);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, workDate)).thenReturn(Optional.of(openAttendance));

        job.markAbsentEmployees();

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void marksOvernightAttendanceMissingAfterShiftEnds() {
        LocalDate workDate = LocalDate.of(2026, 4, 25);
        Clock clock = fixedClock(LocalDateTime.of(2026, 4, 26, 6, 30));
        AbsentCheckJob job = new AbsentCheckJob(
                attendanceRepository,
                employeeScheduleRepository,
                holidayRepository,
                hrClient,
                clock);

        EmployeeSchedule schedule = createSchedule(4L, workDate.getDayOfWeek().getValue(), createShift("Ca đêm", 22, 0, 6, 0), workDate.minusDays(7));
        Attendance openAttendance = Attendance.builder()
                .id(1L)
                .employeeId(4L)
                .workDate(workDate)
                .status("PRESENT")
                .checkInTime(workDate.atTime(22, 5))
                .build();

        when(employeeScheduleRepository.findByEffectiveFromLessThanEqual(any(LocalDate.class))).thenReturn(List.of(schedule));
        when(holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(any(), any())).thenReturn(false);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, workDate)).thenReturn(Optional.of(openAttendance));

        job.markAbsentEmployees();

        verify(attendanceRepository).save(ArgumentMatchers.argThat(attendance ->
                "MISSING_CHECKOUT".equals(attendance.getStatus()) && workDate.equals(attendance.getWorkDate())));
    }

    @Test
    void doesNotMarkAbsentBeforeRegularShiftEnds() {
        LocalDate workDate = LocalDate.of(2026, 4, 25);
        Clock clock = fixedClock(LocalDateTime.of(2026, 4, 25, 9, 0));
        AbsentCheckJob job = new AbsentCheckJob(
                attendanceRepository,
                employeeScheduleRepository,
                holidayRepository,
                hrClient,
                clock);

        EmployeeSchedule schedule = createSchedule(7L, workDate.getDayOfWeek().getValue(), createShift("Ca hành chính", 8, 0, 17, 0), workDate.minusDays(10));

        when(employeeScheduleRepository.findByEffectiveFromLessThanEqual(any(LocalDate.class))).thenReturn(List.of(schedule));
        when(holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(any(), any())).thenReturn(false);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(7L, workDate)).thenReturn(Optional.empty());
        when(hrClient.hasApprovedLeave(7L, workDate)).thenReturn(false);

        job.markAbsentEmployees();

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    private Clock fixedClock(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(BANGKOK).toInstant(), BANGKOK);
    }

    private EmployeeSchedule createSchedule(Long employeeId, Integer dayOfWeek, Shift shift, LocalDate effectiveFrom) {
        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployeeId(employeeId);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setShift(shift);
        schedule.setIsActive(true);
        schedule.setEffectiveFrom(effectiveFrom);
        return schedule;
    }

    private Shift createShift(String name, int startHour, int startMinute, int endHour, int endMinute) {
        Shift shift = new Shift();
        shift.setName(name);
        shift.setStartTime(LocalTime.of(startHour, startMinute));
        shift.setEndTime(LocalTime.of(endHour, endMinute));
        return shift;
    }
}
