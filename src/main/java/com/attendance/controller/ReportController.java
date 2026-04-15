package com.attendance.controller;

import com.attendance.service.AttendanceReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Báo cáo", description = "Xuất báo cáo tổng hợp chấm công")
public class ReportController {

    private final AttendanceReportExportService attendanceReportExportService;

    @GetMapping("/monthly-attendance/export")
    @Operation(summary = "Xuất bảng tổng hợp chấm công theo tháng dạng Excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportMonthlyAttendance(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        YearMonth reportMonth = YearMonth.of(year, month);
        byte[] file = attendanceReportExportService.exportMonthlyExcel(year, month, departmentId, employeeId, includeDetails);
        String filename = employeeId == null
                ? "bang-cong-thang-%02d-%d.xls".formatted(reportMonth.getMonthValue(), reportMonth.getYear())
                : "bang-cong-nhan-vien-%d-thang-%02d-%d.xls".formatted(employeeId, reportMonth.getMonthValue(), reportMonth.getYear());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(file.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }
}
