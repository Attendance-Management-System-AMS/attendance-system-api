package com.attendance.dto.response;

import java.util.Set;

public record InternalUserResponse(
    Long id,
    String username,
    String email,
    boolean enabled,
    Set<String> roles
) {}
