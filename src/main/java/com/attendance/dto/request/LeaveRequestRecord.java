package com.attendance.dto.request;

import java.time.LocalDate;

public record LeaveRequestRecord(
    Long employeeId,
    String leaveTypeCode,
    LocalDate fromDate,
    LocalDate toDate,
    Double totalDays,
    String reason
) {}

