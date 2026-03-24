package com.auth.controller;

import com.auth.dto.*;
import com.auth.service.AuthService;
import com.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Xac thuc", description = "Dang ky, dang nhap, refresh token va dang xuat")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Dang ky tai khoan")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(201, "Dang ky tai khoan thanh cong", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Dang nhap")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(200, "Dang nhap thanh cong", authService.login(request));
    }

    @PostMapping("/introspect")
    @Operation(summary = "Kiem tra token noi bo")
    public ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        return ApiResponse.success(200, "Kiem tra token thanh cong", authService.introspect(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Lay token moi bang refresh token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(200, "Lam moi token thanh cong", authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Dang xuat", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Thieu Authorization header theo dinh dang Bearer <token>");
        }
        authService.logout(token);
        return ApiResponse.success(200, "Dang xuat thanh cong", "OK");
    }

    private String extractToken(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String value = rawValue.trim();
        if (!value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return value.substring(7).trim();
    }
}
