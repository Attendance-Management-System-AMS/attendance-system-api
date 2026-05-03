package com.attendance.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String departmentName,
        String positionName,
        boolean enabled,
        List<String> roles,
        LocalDateTime createdAt
) {
}
