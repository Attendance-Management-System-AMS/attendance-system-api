package com.attendance.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record LeaveApprovalSyncRequest(
        Long employeeId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {}
