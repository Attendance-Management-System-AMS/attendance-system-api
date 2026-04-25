package com.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.dto.request.ApplyTemplateRequest;
import com.attendance.dto.request.EmployeeScheduleRequest;
import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.ScheduleTemplate;
import com.attendance.entity.ScheduleTemplateItem;
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
    void applyTemplateAllowsSameWeekdayWhenEffectiveFromIsDifferent() {
        LocalDate firstEffectiveDate = LocalDate.now().plusDays(30);
        LocalDate nextEffectiveDate = LocalDate.now().plusDays(44);
        Shift adminShift = createShift(1L, "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 0));
        Shift morningShift = createShift(2L, "Ca sáng", LocalTime.of(6, 0), LocalTime.of(14, 0));
        EmployeeSchedule existingSchedule = createSchedule(10L, 5L, adminShift, 6, true, firstEffectiveDate);
        ScheduleTemplate template = createTemplate(20L, createTemplateItem(21L, 6, morningShift));
        ApplyTemplateRequest request = new ApplyTemplateRequest(
                List.of(5L),
                20L,
                nextEffectiveDate,
                nextEffectiveDate.plusDays(7),
                false);

        when(scheduleTemplateRepository.findById(20L)).thenReturn(Optional.of(template));
        when(employeeScheduleRepository.findByEmployeeIdAndIsActiveTrue(5L)).thenReturn(List.of(existingSchedule));
        when(employeeScheduleRepository.saveAll(any())).thenAnswer(invocation -> {
            List<EmployeeSchedule> schedules = invocation.getArgument(0);
            schedules.forEach(schedule -> schedule.setId(99L));
            return schedules;
        });

        List<EmployeeScheduleResponse> responses = scheduleService.applyTemplate(request);

        assertEquals(1, responses.size());
        assertEquals("Ca sáng", responses.get(0).shiftName());
        assertEquals(nextEffectiveDate, responses.get(0).effectiveFrom());
        assertEquals(nextEffectiveDate.plusDays(7), responses.get(0).effectiveTo());
        verify(employeeScheduleRepository, times(1)).saveAll(any());
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
                nextEffectiveDate.plusDays(10),
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
        assertEquals(nextEffectiveDate.plusDays(10), response.effectiveTo());
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
                null,
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
                null,
                false);

        AppException exception = assertThrows(AppException.class, () -> scheduleService.assignSchedule(request));

        assertEquals("Ngày hiệu lực không được ở quá khứ", exception.getMessage());
        verify(shiftRepository, never()).findById(any());
        verify(employeeScheduleRepository, never()).save(any(EmployeeSchedule.class));
    }

    @Test
    void deleteRejectsScheduleThatAlreadyStartedInPast() {
        Shift adminShift = createShift(1L, "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 30));
        EmployeeSchedule existingSchedule = createSchedule(
                10L,
                5L,
                adminShift,
                1,
                true,
                LocalDate.now().minusDays(1));

        when(employeeScheduleRepository.findById(10L)).thenReturn(Optional.of(existingSchedule));

        AppException exception = assertThrows(AppException.class, () -> scheduleService.delete(10L));

        assertEquals("Không thể xóa lịch đã có hiệu lực trong quá khứ", exception.getMessage());
        verify(employeeScheduleRepository, never()).delete(any(EmployeeSchedule.class));
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
        schedule.setEffectiveTo(null);
        return schedule;
    }

    private ScheduleTemplate createTemplate(Long id, ScheduleTemplateItem... items) {
        ScheduleTemplate template = new ScheduleTemplate();
        template.setId(id);
        template.setName("Mẫu lịch");
        for (ScheduleTemplateItem item : items) {
            item.setTemplate(template);
            template.getItems().add(item);
        }
        return template;
    }

    private ScheduleTemplateItem createTemplateItem(Long id, Integer dayOfWeek, Shift shift) {
        ScheduleTemplateItem item = new ScheduleTemplateItem();
        item.setId(id);
        item.setDayOfWeek(dayOfWeek);
        item.setShift(shift);
        return item;
    }
}
