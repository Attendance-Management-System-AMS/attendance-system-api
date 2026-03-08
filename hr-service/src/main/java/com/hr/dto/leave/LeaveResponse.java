package com.hr.dto.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String leaveType,
        LocalDate fromDate,
        LocalDate toDate,
        Double totalDays,
        String reason,
        String status,
        Long approvedBy,
        LocalDateTime createdAt
) {
}
