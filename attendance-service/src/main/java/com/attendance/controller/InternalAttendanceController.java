package com.attendance.controller;

import com.attendance.common.dto.LeaveApprovalSyncRequest;
import com.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/attendance")
@RequiredArgsConstructor
public class InternalAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/leave-approvals/sync")
    public void syncApprovedLeave(@Valid @RequestBody LeaveApprovalSyncRequest request) {
        attendanceService.syncApprovedLeave(request.employeeId(), request.fromDate(), request.toDate());
    }
}
