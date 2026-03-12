package com.hr.controller;

import com.common.dto.ApiResponse;
import com.hr.dto.common.PagingResponse;
import com.hr.dto.employee.EmployeeRequest;
import com.hr.dto.employee.EmployeeResponse;
import com.hr.service.EmployeeService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ApiResponse.success(201, "Tạo nhân viên thành công", response);
    }

    @GetMapping
    public ApiResponse<PagingResponse<EmployeeResponse>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(employeeService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                        @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.update(id, request);
        return ApiResponse.success(200, "Cập nhật nhân viên thành công", response);
    }


}
