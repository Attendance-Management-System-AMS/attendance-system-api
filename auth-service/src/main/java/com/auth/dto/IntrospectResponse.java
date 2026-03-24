package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntrospectResponse {
    private boolean valid;
    private String username;
    private String roles;
    private Instant expiresAt;

    public static IntrospectResponse invalid() {
        return IntrospectResponse.builder().valid(false).build();
    }
}
