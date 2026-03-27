package com.attendance.controller;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.service.AttendanceService;
import com.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Check in/out", description = "Ghi nhận chấm công vào và ra")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in/{employeeId}")
    @Operation(summary = "Check-in nhân viên")
    public ApiResponse<AttendanceResponse> checkIn(
            @Parameter(description = "ID nhân viên")
            @PathVariable Long employeeId) {
        return ApiResponse.success("Check-in thành công", attendanceService.checkIn(employeeId));
    }

    @PostMapping("/check-out/{employeeId}")
    @Operation(summary = "Check-out nhân viên")
    public ApiResponse<AttendanceResponse> checkOut(
            @Parameter(description = "ID nhân viên")
            @PathVariable Long employeeId) {
        return ApiResponse.success("Check-out thành công", attendanceService.checkOut(employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lịch sử chấm công của nhân viên", description = "Trả về danh sách bản ghi chấm công sắp xếp theo ngày giảm dần")
    public ApiResponse<List<AttendanceResponse>> getByEmployee(
            @Parameter(description = "ID nhân viên")
            @PathVariable Long employeeId) {
        return ApiResponse.success("Lấy lịch sử chấm công thành công", attendanceService.getByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/today")
    @Operation(summary = "Chấm công hôm nay của nhân viên")
    public ApiResponse<AttendanceResponse> getTodayAttendance(
            @Parameter(description = "ID nhân viên")
            @PathVariable Long employeeId) {
        return ApiResponse.success("Lấy chấm công hôm nay thành công", attendanceService.getTodayByEmployee(employeeId));
    }

    @GetMapping("/today")
    @Operation(summary = "Danh sách chấm công hôm nay")
    public ApiResponse<List<AttendanceResponse>> getTodayAttendances(
            @Parameter(description = "Ngày cần lấy chấm công (yyyy-MM-dd)")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("Lấy danh sách chấm công thành công", attendanceService.getAttendancesByDate(date));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm chấm công (filter + paging)")
    public ApiResponse<Page<AttendanceResponse>> search(
            @Parameter(description = "ID nhân viên")
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @Parameter(description = "Ngày chấm công chính xác (yyyy-MM-dd). Nếu có date thì from/to bị bỏ qua.")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Trạng thái: PRESENT/LATE/EARLY_LEAVE/ABSENT/...")
            @RequestParam(value = "status", required = false) String status,
            @PageableDefault(size = 20, sort = "workDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(
                "Tìm kiếm chấm công thành công",
                attendanceService.search(employeeId, date, fromDate, toDate, status, pageable));
    }
}
