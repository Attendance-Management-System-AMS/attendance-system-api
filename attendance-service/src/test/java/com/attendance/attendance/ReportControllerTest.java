package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.ReportController;
import com.attendance.dto.response.AttendanceAnnualSummaryResponse;
import com.attendance.dto.response.OvertimeSummaryResponse;
import com.attendance.service.AttendanceReportService;
import com.attendance.service.AttendanceReportExportService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceReportExportService attendanceReportExportService;

    @Mock
    private AttendanceReportService attendanceReportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    void exportMonthlyAttendanceReturnsExcelFile() throws Exception {
        when(attendanceReportExportService.exportMonthlyExcel(eq(2026), eq(4), eq(null), eq(null), eq(true)))
                .thenReturn("excel-data".getBytes());

                mockMvc.perform(get("/api/reports/monthly-attendance/export")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("bang-cong-thang-04-2026.xls")));
    }

    @Test
    void annualAttendanceSummaryReturnsJson() throws Exception {
        when(attendanceReportService.getAnnualAttendanceSummary(eq(2026), eq(null), eq(null)))
                .thenReturn(new AttendanceAnnualSummaryResponse(
                        2026,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        null,
                        null,
                        4,
                        120,
                        8,
                        3,
                        2,
                        5,
                        0,
                        1,
                        0,
                        57600,
                        480,
                        List.of(),
                        List.of()));

        mockMvc.perform(get("/api/reports/attendance/annual-summary").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.year").value(2026))
                .andExpect(jsonPath("$.result.totalEmployees").value(4))
                .andExpect(jsonPath("$.result.workDays").value(120));
    }

    @Test
    void overtimeSummaryReturnsJson() throws Exception {
        when(attendanceReportService.getOvertimeSummary(eq(2026), eq(null), eq(null)))
                .thenReturn(new OvertimeSummaryResponse(
                        2026,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        null,
                        null,
                        2,
                        12,
                        3,
                        7,
                        1,
                        1,
                        1440,
                        1200,
                        900,
                        780,
                        List.of(),
                        List.of()));

        mockMvc.perform(get("/api/reports/overtime-summary").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.year").value(2026))
                .andExpect(jsonPath("$.result.requestCount").value(12))
                .andExpect(jsonPath("$.result.payableMinutes").value(780));
    }

    @Test
    void exportAnnualAttendanceReturnsExcelFile() throws Exception {
        when(attendanceReportExportService.exportAnnualExcel(eq(2026), eq(null), eq(null)))
                .thenReturn("annual-excel".getBytes());

        mockMvc.perform(get("/api/reports/annual-attendance/export").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("bao-cao-cong-nam-2026.xls")));
    }

    @Test
    void exportOvertimeReturnsExcelFile() throws Exception {
        when(attendanceReportExportService.exportOvertimeExcel(eq(2026), eq(null), eq(null)))
                .thenReturn("ot-excel".getBytes());

        mockMvc.perform(get("/api/reports/overtime/export").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("bao-cao-tang-ca-nam-2026.xls")));
    }
}
