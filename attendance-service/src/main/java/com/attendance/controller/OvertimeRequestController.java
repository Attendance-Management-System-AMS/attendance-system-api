package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.OvertimeApprovalRequest;
import com.attendance.dto.request.OvertimeRequestRecord;
import com.attendance.dto.response.OvertimeRequestResponse;
import com.attendance.exception.AppException;
import com.attendance.service.CurrentUserService;
import com.attendance.service.OvertimeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/overtime-requests")
@Tag(name = "Tăng ca", description = "Đăng ký, phê duyệt và thống kê thời gian ngoài giờ")
@RequiredArgsConstructor
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    @Operation(summary = "Đơn tăng ca của tôi")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<PageResponse<OvertimeRequestResponse>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                "Lấy đơn tăng ca của tôi thành công",
                overtimeRequestService.search(null, employeeId, status, fromDate, toDate, pageable));
    }

    @PostMapping("/me")
    @Operation(summary = "Tạo đơn tăng ca cho chính mình")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<OvertimeRequestResponse> createMyRequest(@Valid @RequestBody OvertimeRequestRecord request) {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        return ApiResponse.success(
                201,
                "Gửi đơn tăng ca thành công",
                overtimeRequestService.createForCurrentEmployee(employeeId, request));
    }

    @DeleteMapping("/me/{id}")
    @Operation(summary = "Huỷ đơn tăng ca của tôi")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<OvertimeRequestResponse> cancelMyRequest(@PathVariable Long id) {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        return ApiResponse.success(200, "Huỷ đơn tăng ca thành công", overtimeRequestService.cancel(id, employeeId));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách đơn tăng ca")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<PageResponse<OvertimeRequestResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(overtimeRequestService.search(keyword, employeeId, status, fromDate, toDate, pageable));
    }

    @PostMapping
    @Operation(summary = "Tạo đơn tăng ca cho nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeRequestResponse> createRequest(@Valid @RequestBody OvertimeRequestRecord request) {
        return ApiResponse.success(201, "Tạo đơn tăng ca thành công", overtimeRequestService.createForEmployee(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn tăng ca")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeRequestResponse> getById(@Parameter(description = "ID đơn tăng ca") @PathVariable Long id) {
        return ApiResponse.success(overtimeRequestService.getById(id));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Phê duyệt đơn tăng ca")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeRequestResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) OvertimeApprovalRequest approval) {
        Long approvedById = resolveCurrentEmployeeIdOrNull();
        return ApiResponse.success(200, "Phê duyệt đơn tăng ca thành công", overtimeRequestService.approve(id, approvedById, approval));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đơn tăng ca")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeRequestResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) OvertimeApprovalRequest approval) {
        Long approvedById = resolveCurrentEmployeeIdOrNull();
        return ApiResponse.success(200, "Từ chối đơn tăng ca thành công", overtimeRequestService.reject(id, approvedById, approval));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Huỷ đơn tăng ca đang chờ duyệt")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<OvertimeRequestResponse> cancelByManager(@PathVariable Long id) {
        return ApiResponse.success(200, "Huỷ đơn tăng ca thành công", overtimeRequestService.cancelByManager(id));
    }

    private Long resolveCurrentEmployeeIdOrNull() {
        try {
            return currentUserService.getCurrentEmployeeId();
        } catch (AppException ignored) {
            return null;
        }
    }
}
