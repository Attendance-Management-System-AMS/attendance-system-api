package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.dto.response.AttendanceAnnualSummaryResponse;
import com.attendance.dto.response.OvertimeSummaryResponse;
import com.attendance.service.AttendanceReportService;
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

    private final AttendanceReportService attendanceReportService;
    private final AttendanceReportExportService attendanceReportExportService;

    @GetMapping("/attendance/annual-summary")
    @Operation(summary = "Tổng hợp bảng công theo năm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<AttendanceAnnualSummaryResponse> getAnnualAttendanceSummary(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ApiResponse.success(
                "Lấy báo cáo năm thành công",
                attendanceReportService.getAnnualAttendanceSummary(year, departmentId, employeeId));
    }

    @GetMapping("/overtime-summary")
    @Operation(summary = "Tổng hợp tăng ca theo năm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeSummaryResponse> getOvertimeSummary(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ApiResponse.success(
                "Lấy báo cáo tăng ca thành công",
                attendanceReportService.getOvertimeSummary(year, departmentId, employeeId));
    }

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

    @GetMapping("/annual-attendance/export")
    @Operation(summary = "Xuất bảng tổng hợp chấm công theo năm dạng Excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportAnnualAttendance(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        byte[] file = attendanceReportExportService.exportAnnualExcel(year, departmentId, employeeId);
        String filename = employeeId == null
                ? "bao-cao-cong-nam-%d.xls".formatted(year)
                : "bao-cao-cong-nhan-vien-%d-nam-%d.xls".formatted(employeeId, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(file.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }

    @GetMapping("/overtime/export")
    @Operation(summary = "Xuất báo cáo tăng ca theo năm dạng Excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportOvertimeSummary(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        byte[] file = attendanceReportExportService.exportOvertimeExcel(year, departmentId, employeeId);
        String filename = employeeId == null
                ? "bao-cao-tang-ca-nam-%d.xls".formatted(year)
                : "bao-cao-tang-ca-nhan-vien-%d-nam-%d.xls".formatted(employeeId, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(file.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }
}
