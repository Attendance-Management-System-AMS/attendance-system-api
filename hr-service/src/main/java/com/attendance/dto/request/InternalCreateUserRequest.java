package com.attendance.dto.request;

import java.util.Set;

public record InternalCreateUserRequest(
    String username,
    String password,
    String email,
    boolean enabled,
    Set<String> roles
) {}
