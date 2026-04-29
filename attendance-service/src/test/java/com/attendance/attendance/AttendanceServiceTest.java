package com.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.client.HrClient;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.FaceMatchResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.exception.AppException;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.repository.AttendanceLogRepository;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.HolidayRepository;
import com.attendance.service.AttendanceService;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Mock
    private AttendanceLogRepository attendanceLogRepository;

    @Mock
    private HrClient hrClient;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                attendanceRepository,
                employeeScheduleRepository,
                attendanceLogRepository,
                hrClient,
                attendanceMapper,
                holidayRepository);
        ReflectionTestUtils.setField(attendanceService, "minCheckoutAfterMinutes", 30L);
    }

    @Test
    void syncApprovedLeaveCreatesOnLeaveRecordsWhenAttendanceMissing() {
        LocalDate fromDate = LocalDate.of(2026, 4, 10);
        LocalDate toDate = LocalDate.of(2026, 4, 11);
        Shift shift = createShift(1L);

        when(holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(any(), any())).thenReturn(false);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(4L), any())).thenReturn(Optional.empty());
        when(employeeScheduleRepository.findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(eq(4L), any()))
                .thenReturn(List.of(
                        createSchedule(4L, 5, shift, LocalDate.of(2026, 4, 1)),
                        createSchedule(4L, 6, shift, LocalDate.of(2026, 4, 1))));

        attendanceService.syncApprovedLeave(4L, fromDate, toDate);

        verify(attendanceRepository, times(2)).save(any(Attendance.class));
    }

    @Test
    void syncApprovedLeaveDoesNotOverrideRecordedAttendance() {
        LocalDate workDate = LocalDate.of(2026, 4, 10);
        Attendance recorded = Attendance.builder()
                .id(1L)
                .employeeId(4L)
                .workDate(workDate)
                .status("PRESENT")
                .checkInTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .build();

        when(holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(workDate, workDate)).thenReturn(false);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, workDate)).thenReturn(Optional.of(recorded));

        attendanceService.syncApprovedLeave(4L, workDate, workDate);

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkInRejectsWhenApprovedLeaveExistsForToday() {
        LocalDate today = LocalDate.now();

        when(hrClient.getEmployeeSnapshot(4L)).thenReturn(new HrEmployeeSnapshot(4L, "EMP004", "Pham Thi Employee", "IT", "Dev"));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, today)).thenReturn(Optional.empty());
        when(hrClient.hasApprovedLeave(4L, today)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> attendanceService.checkIn(4L));

        assertEquals("Bạn đang có đơn nghỉ đã được duyệt cho hôm nay", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void scanByFaceRejectsCheckoutTooSoon() {
        LocalDate today = LocalDate.now();
        Attendance existing = Attendance.builder()
                .id(1L)
                .employeeId(4L)
                .workDate(today)
                .status("PRESENT")
                .checkInTime(LocalDateTime.now().minusMinutes(5))
                .build();

        when(hrClient.matchFace(any(FaceDescriptorRequest.class))).thenReturn(new FaceMatchResponse(4L, 0.21d));
        when(hrClient.getEmployeeSnapshot(4L)).thenReturn(new HrEmployeeSnapshot(4L, "EMP004", "Pham Thi Employee", "IT", "Dev"));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, today)).thenReturn(Optional.of(existing));

        AppException exception = assertThrows(
                AppException.class,
                () -> attendanceService.scanByFace(new FaceDescriptorRequest(java.util.Collections.nCopies(128, 0.1d)), "KIOSK-A"));

        assertEquals("Bạn vừa check-in, vui lòng chờ ít nhất 30 phút trước khi check-out", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkInRejectsWhenMultipleSchedulesShareSameEffectiveDate() {
        LocalDate today = LocalDate.now();
        Shift morningShift = createShift(1L);
        Shift eveningShift = createShift(2L);
        eveningShift.setName("Ca tối");
        eveningShift.setStartTime(LocalTime.of(14, 0));
        eveningShift.setEndTime(LocalTime.of(18, 0));

        when(hrClient.getEmployeeSnapshot(4L)).thenReturn(new HrEmployeeSnapshot(4L, "EMP004", "Pham Thi Employee", "IT", "Dev"));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(4L, today)).thenReturn(Optional.empty());
        when(hrClient.hasApprovedLeave(4L, today)).thenReturn(false);
        when(employeeScheduleRepository.findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(4L, today))
                .thenReturn(List.of(
                        createSchedule(4L, today.getDayOfWeek().getValue(), morningShift, today.minusDays(7)),
                        createSchedule(4L, today.getDayOfWeek().getValue(), eveningShift, today.minusDays(7))));

        AppException exception = assertThrows(AppException.class, () -> attendanceService.checkIn(4L));

        assertEquals("Nhân viên đang có nhiều ca làm cùng ngày hiệu lực, hệ thống chưa hỗ trợ split shift", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    private Shift createShift(Long id) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setName("Ca hành chính");
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        return shift;
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
}
