package com.attendance.controller;

import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.request.*;
import com.attendance.dto.response.*;
import com.attendance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "Đăng nhập, refresh token, đăng xuất và hồ sơ người dùng")
public class AuthController {

    private final AuthService authService;

    // Đăng nhập bằng username hoặc email và trả về cặp token.
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(200, "Đăng nhập thành công", authService.login(request));
    }

    // Đổi refresh token cũ lấy access token mới.
    @PostMapping("/refresh")
    @Operation(summary = "Lấy token mới bằng refresh token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(200, "Làm mới token thành công", authService.refresh(request));
    }

    // Đăng xuất bằng cách đưa token hiện tại vào blacklist.
    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        authService.logout(authHeader);
        return ApiResponse.success(200, "Đăng xuất thành công", "OK");
    }

    // Lấy thông tin tài khoản đang đăng nhập.
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(200, "Lấy thông tin người dùng thành công", authService.getCurrentUser());
    }

    // Đổi mật khẩu cho tài khoản hiện tại.
    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success(200, "Đổi mật khẩu thành công", "OK");
    }
}




