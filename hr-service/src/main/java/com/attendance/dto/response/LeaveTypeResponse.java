package com.attendance.dto.response;

public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        Boolean isPaid,
        Boolean isActive,
        String description
) {
}
