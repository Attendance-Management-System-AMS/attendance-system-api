package com.attendance.dto.response;

import java.time.Instant;

public record KioskSessionResponse(
        String token,
        String tokenType,
        String deviceId,
        long expiresIn,
        Instant expiresAt
) {}
