package com.attendance.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.attendance.common.dto.PageResponse;
import com.attendance.controller.AttendanceController;
import com.attendance.controller.ReportController;
import com.attendance.controller.ShiftController;
import com.attendance.dto.response.ShiftResponse;
import com.attendance.dto.response.AttendanceAnnualSummaryResponse;
import com.attendance.service.AttendanceReportExportService;
import com.attendance.service.AttendanceReportService;
import com.attendance.service.AttendanceService;
import com.attendance.service.CurrentUserService;
import com.attendance.service.KioskAccessService;
import com.attendance.service.ShiftService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(ControllerAuthorizationTest.TestConfig.class)
class ControllerAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        AttendanceController attendanceController(
                AttendanceService attendanceService,
                CurrentUserService currentUserService,
                KioskAccessService kioskAccessService) {
            return new AttendanceController(attendanceService, currentUserService, kioskAccessService);
        }

        @Bean
        ReportController reportController(
                AttendanceReportService attendanceReportService,
                AttendanceReportExportService attendanceReportExportService) {
            return new ReportController(attendanceReportService, attendanceReportExportService);
        }

        @Bean
        ShiftController shiftController(ShiftService shiftService) {
            return new ShiftController(shiftService);
        }

        @Bean AttendanceService attendanceService() { return mock(AttendanceService.class); }
        @Bean CurrentUserService currentUserService() { return mock(CurrentUserService.class); }
        @Bean KioskAccessService kioskAccessService() { return mock(KioskAccessService.class); }
        @Bean AttendanceReportService attendanceReportService() { return mock(AttendanceReportService.class); }
        @Bean AttendanceReportExportService attendanceReportExportService() { return mock(AttendanceReportExportService.class); }
        @Bean ShiftService shiftService() { return mock(ShiftService.class); }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private AttendanceController attendanceController;

    @org.springframework.beans.factory.annotation.Autowired
    private ReportController reportController;

    @org.springframework.beans.factory.annotation.Autowired
    private ShiftController shiftController;

    @org.springframework.beans.factory.annotation.Autowired
    private AttendanceService attendanceService;

    @org.springframework.beans.factory.annotation.Autowired
    private CurrentUserService currentUserService;

    @org.springframework.beans.factory.annotation.Autowired
    private AttendanceReportService attendanceReportService;

    @org.springframework.beans.factory.annotation.Autowired
    private ShiftService shiftService;

    @Test
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void employeeCannotReadAnnualAttendanceSummary() {
        assertThrows(AccessDeniedException.class, () -> reportController.getAnnualAttendanceSummary(2026, null, null));
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void managerCanReadAnnualAttendanceSummary() {
        when(attendanceReportService.getAnnualAttendanceSummary(eq(2026), isNull(), isNull()))
                .thenReturn(new AttendanceAnnualSummaryResponse(
                        2026,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        null,
                        null,
                        1,
                        10,
                        1,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        4800,
                        120,
                        List.of(),
                        List.of()));

        var response = reportController.getAnnualAttendanceSummary(2026, null, null);

        assertThat(response.result()).isNotNull();
        assertThat(response.result().year()).isEqualTo(2026);
    }

    @Test
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void employeeCanReadOwnAttendance() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(12L);
        when(attendanceService.search(eq(12L), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageResponse<>(List.of(), 1, 20, 0, 0));

        var response = attendanceController.getMyAttendance(0, 20, "workDate", "desc", null, null, null, null);

        assertThat(response.result()).isNotNull();
        assertThat(response.result().content()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void employeeCannotSearchCompanyAttendance() {
        assertThrows(
                AccessDeniedException.class,
                () -> attendanceController.search(0, 20, "workDate", "desc", null, null, null, null, null));
    }

    @Test
    @WithMockUser(authorities = "ROLE_MANAGER")
    void managerCannotAccessEmployeeDirectory() {
        assertThrows(
                AccessDeniedException.class,
                () -> shiftController.getShifts(0, 20, "startTime", "asc", null));
    }

    @Test
    @WithMockUser(authorities = "ROLE_HR")
    void hrCanAccessShiftDirectory() {
        when(shiftService.search(isNull(), any()))
                .thenReturn(new PageResponse<>(
                        List.of(new ShiftResponse(
                                1L,
                                "Ca hành chính",
                                LocalTime.of(8, 0),
                                LocalTime.of(17, 0),
                                LocalTime.of(12, 0),
                                LocalTime.of(13, 0),
                                10,
                                LocalDateTime.now())),
                        1,
                        20,
                        1,
                        1));

        var response = shiftController.getShifts(0, 20, "startTime", "asc", null);

        assertThat(response.result().content()).hasSize(1);
    }
}
