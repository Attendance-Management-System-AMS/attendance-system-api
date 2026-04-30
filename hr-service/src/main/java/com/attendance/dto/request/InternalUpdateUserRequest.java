package com.attendance.dto.request;

public record InternalUpdateUserRequest(
    String username,
    String email,
    boolean enabled
) {}
