package com.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.dto.request.EmployeeScheduleRequest;
import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.exception.AppException;
import com.attendance.mapper.EmployeeScheduleMapper;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.ScheduleTemplateRepository;
import com.attendance.repository.ShiftRepository;
import com.attendance.service.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ScheduleTemplateRepository scheduleTemplateRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                employeeScheduleRepository,
                shiftRepository,
                scheduleTemplateRepository,
                new EmployeeScheduleMapper());
    }

    @Test
    void assignScheduleAllowsOverlappingShiftWhenEffectiveFromIsDifferent() {
        LocalDate firstEffectiveDate = LocalDate.now().plusDays(30);
        LocalDate nextEffectiveDate = LocalDate.now().plusDays(60);
        Shift adminShift = createShift(1L, "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 30));
        EmployeeSchedule existingSchedule = createSchedule(10L, 5L, adminShift, 1, true, firstEffectiveDate);
        EmployeeScheduleRequest request = new EmployeeScheduleRequest(
                5L,
                1L,
                1,
                true,
                nextEffectiveDate,
                false);

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(adminShift));
        when(employeeScheduleRepository.findByEmployeeIdAndIsActiveTrue(5L)).thenReturn(List.of(existingSchedule));
        when(employeeScheduleRepository.save(any(EmployeeSchedule.class))).thenAnswer(invocation -> {
            EmployeeSchedule schedule = invocation.getArgument(0);
            schedule.setId(99L);
            return schedule;
        });

        EmployeeScheduleResponse response = scheduleService.assignSchedule(request);

        assertEquals(99L, response.id());
        assertEquals(nextEffectiveDate, response.effectiveFrom());
        assertEquals("Ca hành chính", response.shiftName());
        verify(employeeScheduleRepository, times(1)).save(any(EmployeeSchedule.class));
    }

    @Test
    void assignScheduleRejectsOverlappingShiftWhenEffectiveFromIsSame() {
        LocalDate effectiveDate = LocalDate.now().plusDays(30);
        Shift adminShift = createShift(1L, "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 30));
        EmployeeSchedule existingSchedule = createSchedule(10L, 5L, adminShift, 1, true, effectiveDate);
        EmployeeScheduleRequest request = new EmployeeScheduleRequest(
                5L,
                1L,
                1,
                true,
                effectiveDate,
                false);

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(adminShift));
        when(employeeScheduleRepository.findByEmployeeIdAndIsActiveTrue(5L)).thenReturn(List.of(existingSchedule));

        AppException exception = assertThrows(AppException.class, () -> scheduleService.assignSchedule(request));

        assertEquals("Phát hiện xung đột lịch làm", exception.getMessage());
        verify(employeeScheduleRepository, never()).save(any(EmployeeSchedule.class));
    }

    @Test
    void assignScheduleRejectsPastEffectiveFrom() {
        EmployeeScheduleRequest request = new EmployeeScheduleRequest(
                5L,
                1L,
                1,
                true,
                LocalDate.now().minusDays(1),
                false);

        AppException exception = assertThrows(AppException.class, () -> scheduleService.assignSchedule(request));

        assertEquals("Ngày hiệu lực không được ở quá khứ", exception.getMessage());
        verify(shiftRepository, never()).findById(any());
        verify(employeeScheduleRepository, never()).save(any(EmployeeSchedule.class));
    }

    private Shift createShift(Long id, String name, LocalTime startTime, LocalTime endTime) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setName(name);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setCreatedAt(LocalDateTime.of(2026, 4, 22, 0, 0));
        return shift;
    }

    private EmployeeSchedule createSchedule(
            Long id,
            Long employeeId,
            Shift shift,
            Integer dayOfWeek,
            Boolean isActive,
            LocalDate effectiveFrom) {
        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setId(id);
        schedule.setEmployeeId(employeeId);
        schedule.setShift(shift);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setIsActive(isActive);
        schedule.setEffectiveFrom(effectiveFrom);
        return schedule;
    }
}
