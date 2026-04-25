package com.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.entity.Shift;
import com.attendance.exception.AppException;
import com.attendance.mapper.ShiftMapper;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.ScheduleTemplateItemRepository;
import com.attendance.repository.ShiftRepository;
import com.attendance.service.ShiftService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Mock
    private ScheduleTemplateItemRepository scheduleTemplateItemRepository;

    private ShiftService shiftService;

    @BeforeEach
    void setUp() {
        shiftService = new ShiftService(
                shiftRepository,
                employeeScheduleRepository,
                scheduleTemplateItemRepository,
                Mappers.getMapper(ShiftMapper.class));
    }

    @Test
    void deleteRejectsShiftInUse() {
        Shift shift = createShift(7L, "Ca sáng");
        when(shiftRepository.findById(7L)).thenReturn(Optional.of(shift));
        when(employeeScheduleRepository.existsByShift_Id(7L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> shiftService.delete(7L));

        assertEquals("Không thể xóa ca làm đang được sử dụng trong lịch làm việc hoặc mẫu lịch", exception.getMessage());
        verify(scheduleTemplateItemRepository, never()).existsByShift_Id(7L);
        verify(shiftRepository, never()).delete(shift);
    }

    @Test
    void deleteAllowsUnusedShift() {
        Shift shift = createShift(9L, "Ca tối");
        when(shiftRepository.findById(9L)).thenReturn(Optional.of(shift));
        when(employeeScheduleRepository.existsByShift_Id(9L)).thenReturn(false);
        when(scheduleTemplateItemRepository.existsByShift_Id(9L)).thenReturn(false);

        shiftService.delete(9L);

        verify(shiftRepository).delete(shift);
    }

    private Shift createShift(Long id, String name) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setName(name);
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setCreatedAt(LocalDateTime.of(2026, 4, 25, 0, 0));
        return shift;
    }
}
