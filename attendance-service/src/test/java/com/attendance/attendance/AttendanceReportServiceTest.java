package com.attendance.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.attendance.client.HrClient;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.OvertimeSummaryResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.OvertimeRequest;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.OvertimeRequestRepository;
import com.attendance.service.AttendanceReportService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceReportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private OvertimeRequestRepository overtimeRequestRepository;

    @Mock
    private HrClient hrClient;

    private AttendanceReportService attendanceReportService;

    @BeforeEach
    void setUp() {
        attendanceReportService = new AttendanceReportService(
                attendanceRepository,
                overtimeRequestRepository,
                hrClient,
                Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneId.of("Asia/Bangkok")));
    }

    @Test
    void annualSummaryIncludesEmployeeFoundOnlyFromHistoricalAttendance() {
        Attendance attendance = Attendance.builder()
                .employeeId(99L)
                .workDate(LocalDate.of(2026, 2, 3))
                .checkInTime(LocalDateTime.of(2026, 2, 3, 8, 1))
                .checkOutTime(LocalDateTime.of(2026, 2, 3, 17, 0))
                .status("PRESENT")
                .workedMinutes(480)
                .payableOvertimeMinutes(60)
                .build();

        when(attendanceRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(attendance));
        when(hrClient.findEmployeeSnapshots(null, null, "ACTIVE"))
                .thenReturn(List.of(new HrEmployeeSnapshot(1L, "EMP-0001", "Nhân viên Active", "HR", "HR")));
        when(hrClient.findEmployeeSnapshotsByIds(List.of(99L)))
                .thenReturn(List.of(new HrEmployeeSnapshot(99L, "EMP-0099", "Nhân viên Lịch sử", "IT", "Dev")));

        var summary = attendanceReportService.getAnnualAttendanceSummary(2026, null, null);

        assertThat(summary.totalEmployees()).isEqualTo(2);
        assertThat(summary.workDays()).isEqualTo(1);
        assertThat(summary.workedMinutes()).isEqualTo(480);
        assertThat(summary.overtimeMinutes()).isEqualTo(60);
        assertThat(summary.employees())
                .extracting(employee -> employee.employeeId())
                .contains(99L);
    }

    @Test
    void overtimeSummaryAggregatesRequestsAndAttendance() {
        OvertimeRequest approved = new OvertimeRequest();
        approved.setEmployeeId(10L);
        approved.setWorkDate(LocalDate.of(2026, 3, 10));
        approved.setRequestedMinutes(120);
        approved.setStatus("APPROVED");
        approved.setStartTime(LocalTime.of(18, 0));
        approved.setEndTime(LocalTime.of(20, 0));

        OvertimeRequest pending = new OvertimeRequest();
        pending.setEmployeeId(10L);
        pending.setWorkDate(LocalDate.of(2026, 4, 12));
        pending.setRequestedMinutes(60);
        pending.setStatus("PENDING");
        pending.setStartTime(LocalTime.of(18, 0));
        pending.setEndTime(LocalTime.of(19, 0));

        Attendance marchAttendance = Attendance.builder()
                .employeeId(10L)
                .workDate(LocalDate.of(2026, 3, 10))
                .actualOvertimeMinutes(110)
                .approvedOvertimeMinutes(120)
                .payableOvertimeMinutes(90)
                .build();

        Attendance aprilAttendance = Attendance.builder()
                .employeeId(10L)
                .workDate(LocalDate.of(2026, 4, 12))
                .actualOvertimeMinutes(45)
                .approvedOvertimeMinutes(0)
                .payableOvertimeMinutes(0)
                .build();

        when(overtimeRequestRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(approved, pending));
        when(attendanceRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(marchAttendance, aprilAttendance));
        when(hrClient.findEmployeeSnapshotsByIds(List.of(10L)))
                .thenReturn(List.of(new HrEmployeeSnapshot(10L, "EMP-0010", "Nguyễn Văn A", "IT", "Dev")));

        OvertimeSummaryResponse summary = attendanceReportService.getOvertimeSummary(2026, null, null);

        assertThat(summary.totalEmployees()).isEqualTo(1);
        assertThat(summary.requestCount()).isEqualTo(2);
        assertThat(summary.pendingRequests()).isEqualTo(1);
        assertThat(summary.approvedRequests()).isEqualTo(1);
        assertThat(summary.requestedMinutes()).isEqualTo(180);
        assertThat(summary.approvedMinutes()).isEqualTo(120);
        assertThat(summary.actualMinutes()).isEqualTo(155);
        assertThat(summary.payableMinutes()).isEqualTo(90);
        assertThat(summary.employees().get(0).fullName()).isEqualTo("Nguyễn Văn A");
    }
}
