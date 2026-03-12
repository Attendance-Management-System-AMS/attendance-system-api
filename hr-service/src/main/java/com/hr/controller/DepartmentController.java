package com.hr.controller;

import com.common.dto.ApiResponse;
import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.hr.service.DepartmentService;
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
@RequestMapping("/api/hr/departments")
@Tag(name = "HR - Phòng ban", description = "Quản lý danh mục phòng ban")
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
    @Operation(summary = "Lấy danh sách phòng ban")
    public ApiResponse<List<DepartmentResponse>> getDepartments() {
        return ApiResponse.success(departmentService.getAll());
    }
}
