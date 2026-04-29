package com.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record LeaveResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeCode,
        String departmentName,
        String positionName,
        String leaveTypeCode,
        String leaveTypeName,
        LocalDate fromDate,
        LocalDate toDate,
        Double totalDays,
        String reason,
        String status,
        String approvedByName,
        LocalDateTime createdAt,
        // Dùng cho đơn giải trình công (loại AC)
        LocalTime correctedCheckIn,
        LocalTime correctedCheckOut
) {
}

