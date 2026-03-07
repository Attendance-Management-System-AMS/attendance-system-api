package com.hr.controller;

import com.hr.dto.common.ApiResponse;
import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.hr.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.create(request);
        return ApiResponse.success(201, "Tạo phòng ban thành công", response);
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> getDepartments() {
        return ApiResponse.success(departmentService.getAll());
    }
}
