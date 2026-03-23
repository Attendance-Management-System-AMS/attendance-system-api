package com.hr.controller;

import com.common.dto.ApiResponse;
import com.hr.dto.leave.LeaveRequestRecord;
import com.hr.dto.leave.LeaveResponse;
import com.hr.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/leaves")
@Tag(name = "HR - Nghỉ phép", description = "Quản lý đơn nghỉ phép của nhân viên")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Tạo đơn xin nghỉ")
    public ApiResponse<LeaveResponse> createRequest(@Valid @RequestBody LeaveRequestRecord request) {
        LeaveResponse response = leaveService.createRequest(request);
        return ApiResponse.success(201, "Gửi đơn xin nghỉ thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả đơn nghỉ", description = "Lọc theo trạng thái: PENDING, APPROVED, REJECTED")
    public ApiResponse<List<LeaveResponse>> getAll(
            @Parameter(description = "Trạng thái đơn (không bắt buộc)")
            @RequestParam(required = false) String status) {
        return ApiResponse.success(leaveService.getAll(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đơn nghỉ")
    public ApiResponse<LeaveResponse> getById(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(leaveService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lấy danh sách đơn nghỉ theo nhân viên")
    public ApiResponse<List<LeaveResponse>> getByEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success(leaveService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Phê duyệt đơn nghỉ")
    public ApiResponse<LeaveResponse> approve(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id,
            @Parameter(description = "ID người phê duyệt (không bắt buộc)")
            @RequestParam(required = false) Long approvedById) {
        return ApiResponse.success(200, "Phê duyệt đơn nghỉ thành công", leaveService.approve(id, approvedById));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đơn nghỉ")
    public ApiResponse<LeaveResponse> reject(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(200, "Từ chối đơn nghỉ thành công", leaveService.reject(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Huỷ đơn nghỉ", description = "Chỉ huỷ đơn đang ở trạng thái PENDING")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        leaveService.delete(id);
        return ApiResponse.success(200, "Huỷ đơn nghỉ thành công", null);
    }
}
