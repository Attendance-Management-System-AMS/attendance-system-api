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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/leaves")
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

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lấy danh sách đơn nghỉ theo nhân viên")
    public ApiResponse<List<LeaveResponse>> getByEmployee(
            @Parameter(description = "ID nhân viên", example = "12")
            @PathVariable Long employeeId) {
        return ApiResponse.success(leaveService.getByEmployee(employeeId));
    }
}
