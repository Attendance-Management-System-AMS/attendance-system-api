package com.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        LocalDate workDate,
        String status,
        LocalDateTime createdAt
) {
}
