package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.dto.request.ShiftRequest;
import com.attendance.dto.response.ShiftResponse;
import com.attendance.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.attendance.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Danh mục ca làm", description = "Quản lý các loại ca làm việc trong hệ thống")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
public class ShiftController {

    private final ShiftService shiftService;

    // Tạo mới một ca làm việc.
    @PostMapping
    @Operation(summary = "Tạo mới ca làm")
    public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.create(request);
        return ApiResponse.success("Tạo ca làm thành công", response);
    }

    // Lấy danh sách ca làm việc (có phân trang và tìm kiếm).
    @GetMapping
    @Operation(summary = "Lấy danh sách ca làm (phân trang, lọc)",
            description = "Lọc theo từ khoá tên. Nếu không truyền page/size sẽ dùng giá trị mặc định.")
    public ApiResponse<PageResponse<ShiftResponse>> getShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Từ khoá theo tên ca làm") @RequestParam(value = "keyword", required = false) String keyword) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success("Lấy danh sách ca làm thành công", shiftService.search(keyword, pageable));
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



