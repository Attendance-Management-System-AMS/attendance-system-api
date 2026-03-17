package com.hr.controller;

import com.common.dto.ApiResponse;
import com.hr.dto.common.PagingResponse;
import com.hr.dto.employee.EmployeeRequest;
import com.hr.dto.employee.EmployeeResponse;
import com.hr.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
@RequestMapping("/api/hr/employees")
@Tag(name = "HR - Nhân viên", description = "Quản lý hồ sơ nhân viên")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Tạo mới nhân viên")
    public ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ApiResponse.success(201, "Tạo nhân viên thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách nhân viên", description = "Hỗ trợ phân trang bằng page và size")
    public ApiResponse<PagingResponse<EmployeeResponse>> getEmployees(
            @Parameter(description = "Trang hiện tại", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(employeeService.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết nhân viên theo ID")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin nhân viên")
    public ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                        @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.update(id, request);
        return ApiResponse.success(200, "Cập nhật nhân viên thành công", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Vô hiệu hoá nhân viên", description = "Đặt trạng thái nhân viên thành INACTIVE")
    public ApiResponse<Void> deleteEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.success(200, "Vô hiệu hoá nhân viên thành công", null);
    }


}
