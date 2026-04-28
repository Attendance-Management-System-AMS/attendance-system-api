package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.ReportController;
import com.attendance.service.AttendanceReportExportService;
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
}
