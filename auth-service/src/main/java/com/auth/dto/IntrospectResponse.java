package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntrospectResponse {
    private boolean valid;
    private String username;
    private List<String> roles;
    private Instant expiresAt;

    // Tạo response mặc định khi token không hợp lệ.
    public static IntrospectResponse invalid() {
        return IntrospectResponse.builder().valid(false).build();
    }
}
