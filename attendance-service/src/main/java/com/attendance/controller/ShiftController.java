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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/shifts")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Ca làm", description = "Quản lý danh mục ca làm việc")
public class ShiftController {

    private final ShiftService shiftService;

    // Tạo mới một ca làm việc.
    @PostMapping
    @Operation(summary = "Tạo mới ca làm")
    public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.create(request);
        return ApiResponse.success("Tạo ca làm thành công", response);
    }

    // Lấy toàn bộ danh sách ca làm.
    @GetMapping
    @Operation(summary = "Lấy danh sách ca làm")
    public ApiResponse<List<ShiftResponse>> getShifts() {
        return ApiResponse.success("Lấy danh sách ca làm thành công", shiftService.getAll());
    }

    // Tìm kiếm ca làm theo từ khoá và phân trang.
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm ca làm (filter + paging)")
    public ApiResponse<Page<ShiftResponse>> search(
            @Parameter(description = "Từ khoá theo tên ca làm")
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success("Tìm kiếm ca làm thành công", shiftService.search(keyword, pageable));
    }

    // Lấy chi tiết ca làm theo ID.
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết ca làm theo ID")
    public ApiResponse<ShiftResponse> getShiftById(
            @Parameter(description = "ID ca làm") @PathVariable Long id) {
        return ApiResponse.success("Lấy ca làm thành công", shiftService.getById(id));
    }

    // Cập nhật thông tin ca làm.
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ca làm")
    public ApiResponse<ShiftResponse> updateShift(
            @Parameter(description = "ID ca làm") @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        return ApiResponse.success("Cập nhật ca làm thành công", shiftService.update(id, request));
    }

    // Xóa ca làm theo ID.
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ca làm")
    public ApiResponse<Void> deleteShift(
            @Parameter(description = "ID ca làm") @PathVariable Long id) {
        shiftService.delete(id);
        return ApiResponse.success("Xóa ca làm thành công", null);
    }
}
