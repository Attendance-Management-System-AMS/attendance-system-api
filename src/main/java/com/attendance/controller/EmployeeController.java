package com.attendance.controller;

import com.attendance.dto.request.*;
import com.attendance.dto.response.*;
import com.attendance.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/employees")
@Tag(name = "HR - Nhân viên", description = "Quản lý hồ sơ nhân viên")
public class EmployeeController {

    private final EmployeeService employeeService;

    // Khởi tạo controller với service xử lý nhân viên.
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Tạo mới một hồ sơ nhân viên.
    @PostMapping
    @Operation(summary = "Tạo mới nhân viên")
    public ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ApiResponse.success(201, "Tạo nhân viên thành công", response);
    }

    // Lấy danh sách nhân viên theo phân trang và bộ lọc.
    @GetMapping
    @Operation(summary = "Lấy danh sách nhân viên (phân trang, lọc)")
    public ApiResponse<PageResponse<EmployeeResponse>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                employeeService.search(keyword, departmentId, positionId, status, pageable));
    }

    // Lấy chi tiết nhân viên theo ID.
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết nhân viên theo ID")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    // Cập nhật thông tin nhân viên.
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin nhân viên")
    public ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                        @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.update(id, request);
        return ApiResponse.success(200, "Cập nhật nhân viên thành công", response);
    }

    // Lưu descriptor khuôn mặt của nhân viên.
    @PutMapping("/{id}/face-descriptor")
    @Operation(summary = "Đăng ký / cập nhật descriptor khuôn mặt (face-api.js, 128 float)")
    public ApiResponse<EmployeeResponse> registerFaceDescriptor(
            @PathVariable Long id,
            @Valid @RequestBody FaceDescriptorRequest request) {
        return ApiResponse.success(200, "Đăng ký khuôn mặt thành công", employeeService.registerFaceEmbedding(id, request));
    }

    // So khớp khuôn mặt với nhân viên đã đăng ký.
    @PostMapping("/match-face")
    @Operation(summary = "So khớp descriptor với nhân viên đã đăng ký (Euclidean, ngưỡng cấu hình app.face-match)")
    public ApiResponse<FaceMatchResponse> matchFace(@Valid @RequestBody FaceDescriptorRequest request) {
        return ApiResponse.success("Khớp khuôn mặt", employeeService.matchFace(request));
    }

    // Lấy thông tin nhân viên (internal) theo userId để phục vụ các service khác (ví dụ: auth-service).
    @GetMapping("/internal/user/{userId}")
    @Operation(summary = "Lấy thông tin nhân viên theo userId (Internal call)", hidden = true)
    public EmployeeInternalResponse getInternalEmployee(@PathVariable Long userId) {
        EmployeeResponse employee = employeeService.getByUserId(userId);
        return EmployeeInternalResponse.builder()
                .userId(userId)
                .fullName(employee.fullName())
                .departmentName(employee.departmentName())
                .positionName(employee.positionName())
                .build();
    }

    // Vô hiệu hoá nhân viên thay vì xoá cứng.
    @DeleteMapping("/{id}")
    @Operation(summary = "Vô hiệu hoá nhân viên", description = "Đặt trạng thái nhân viên thành INACTIVE")
    public ApiResponse<Void> deleteEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.success(200, "Vô hiệu hoá nhân viên thành công", null);
    }
}




