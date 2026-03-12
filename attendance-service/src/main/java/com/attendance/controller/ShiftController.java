package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.shift.ShiftRequest;
import com.attendance.dto.shift.ShiftResponse;
import com.attendance.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/shifts")
@Tag(name = "Chấm công - Ca làm", description = "Quản lý danh mục ca làm việc")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @Operation(summary = "Tạo mới ca làm")
    public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.create(request);
        return ApiResponse.success(201, "Tạo ca làm thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách ca làm")
    public ApiResponse<List<ShiftResponse>> getShifts() {
        return ApiResponse.success(shiftService.getAll());
    }
}
