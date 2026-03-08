package com.hr.controller;

import com.hr.dto.common.ApiResponse;
import com.hr.dto.leave.LeaveRequestRecord;
import com.hr.dto.leave.LeaveResponse;
import com.hr.service.LeaveService;
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
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ApiResponse<LeaveResponse> createRequest(@Valid @RequestBody LeaveRequestRecord request) {
        LeaveResponse response = leaveService.createRequest(request);
        return ApiResponse.success(201, "Gửi đơn xin nghỉ thành công", response);
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<LeaveResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ApiResponse.success(leaveService.getByEmployee(employeeId));
    }
}
