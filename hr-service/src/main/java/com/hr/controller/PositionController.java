package com.hr.controller;

import com.common.dto.ApiResponse;
import com.common.pagination.PageResponse;
import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@RequestMapping("/api/positions")
@Tag(name = "HR - Chức vụ", description = "Quản lý chức vụ nhân sự")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách chức vụ (phân trang, lọc)",
            description = "Lọc theo từ khoá tên và/hoặc departmentId")
    public ApiResponse<PageResponse<PositionResponse>> getPositions(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "ID phòng ban (không bắt buộc)") @RequestParam(required = false) Long departmentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success(positionService.search(keyword, departmentId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết chức vụ theo ID")
    public ApiResponse<PositionResponse> getPositionById(
            @Parameter(description = "ID chức vụ") @PathVariable Long id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo mới chức vụ")
    public ApiResponse<PositionResponse> createPosition(@Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.create(request);
        return ApiResponse.success(201, "Tạo chức vụ thành công", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật chức vụ")
    public ApiResponse<PositionResponse> updatePosition(
            @Parameter(description = "ID chức vụ") @PathVariable Long id,
            @Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.update(id, request);
        return ApiResponse.success(200, "Cập nhật chức vụ thành công", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa chức vụ")
    public ApiResponse<Void> deletePosition(
            @Parameter(description = "ID chức vụ") @PathVariable Long id) {
        positionService.delete(id);
        return ApiResponse.success(200, "Xóa chức vụ thành công", null);
    }
}
