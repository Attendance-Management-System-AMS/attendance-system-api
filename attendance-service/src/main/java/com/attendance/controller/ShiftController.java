package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.shift.ShiftRequest;
import com.attendance.dto.shift.ShiftResponse;
import com.attendance.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/shifts")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Ca làm", description = "Quản lý danh mục ca làm việc")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @Operation(summary = "Tạo mới ca làm")
    public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.create(request);
        return ApiResponse.success("Tạo ca làm thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách ca làm")
    public ApiResponse<List<ShiftResponse>> getShifts() {
        return ApiResponse.success("Lấy danh sách ca làm thành công", shiftService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết ca làm theo ID")
    public ApiResponse<ShiftResponse> getShiftById(
            @Parameter(description = "ID ca làm") @PathVariable Long id) {
        return ApiResponse.success("Lấy ca làm thành công", shiftService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ca làm")
    public ApiResponse<ShiftResponse> updateShift(
            @Parameter(description = "ID ca làm") @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        return ApiResponse.success("Cập nhật ca làm thành công", shiftService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ca làm")
    public ApiResponse<Void> deleteShift(
            @Parameter(description = "ID ca làm") @PathVariable Long id) {
        shiftService.delete(id);
        return ApiResponse.success("Xóa ca làm thành công", null);
    }
}
