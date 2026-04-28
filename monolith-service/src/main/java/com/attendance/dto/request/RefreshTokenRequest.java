package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @Schema(description = "Refresh token", example = "eyJhbGciOi...")
    @NotBlank(message = "Refresh token la bat buoc")
    private String refreshToken;
}




