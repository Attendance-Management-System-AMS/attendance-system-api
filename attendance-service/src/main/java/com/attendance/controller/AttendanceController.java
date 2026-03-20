package com.attendance.controller;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.service.AttendanceService;
import com.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

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
}
