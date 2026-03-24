package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntrospectRequest {
    @Schema(description = "Token can kiem tra", example = "eyJhbGciOi...")
    @NotBlank(message = "Token la bat buoc")
    private String token;
}
