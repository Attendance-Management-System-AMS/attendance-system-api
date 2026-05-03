package com.attendance.dto.response;

public record RoleSummaryResponse(
        String roleName,
        String description,
        long userCount
) {
}
