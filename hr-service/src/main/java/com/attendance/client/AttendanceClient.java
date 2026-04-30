package com.attendance.client;

import com.attendance.common.dto.AttendanceCorrectionSyncRequest;
import com.attendance.common.dto.LeaveApprovalSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "attendance-service")
public interface AttendanceClient {

    @PostMapping("/internal/attendance/leave-approvals/sync")
    void syncApprovedLeave(@RequestBody LeaveApprovalSyncRequest request);

    @PostMapping("/internal/attendance/corrections/sync")
    void syncAttendanceCorrection(@RequestBody AttendanceCorrectionSyncRequest request);
}

