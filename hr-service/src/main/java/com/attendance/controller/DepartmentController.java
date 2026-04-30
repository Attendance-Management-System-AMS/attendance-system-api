package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.DepartmentRequest;
import com.attendance.dto.response.DepartmentResponse;
import com.attendance.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/departments")
@Tag(name = "Phòng ban", description = "Quản lý danh mục phòng ban")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @Operation(summary = "Tạo mới phòng ban")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.create(request);
        return ApiResponse.success(201, "Tạo phòng ban thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách phòng ban (phân trang, lọc)")
    public ApiResponse<PageResponse<DepartmentResponse>> getDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(departmentService.getList(keyword, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phòng ban theo ID")
    public ApiResponse<DepartmentResponse> getDepartmentById(
            @Parameter(description = "ID phòng ban") @PathVariable Long id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phòng ban")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @Parameter(description = "ID phòng ban") @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.update(id, request);
        return ApiResponse.success(200, "Cập nhật phòng ban thành công", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phòng ban")
    public ApiResponse<Void> deleteDepartment(
            @Parameter(description = "ID phòng ban") @PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.success(200, "Xóa phòng ban thành công", null);
    }
}



