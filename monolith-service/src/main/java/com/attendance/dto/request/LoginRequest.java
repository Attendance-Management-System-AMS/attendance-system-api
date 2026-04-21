package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "Tên đăng nhập", example = "admin")
    private String username;

    @Schema(description = "Email", example = "admin@company.com")
    private String email;

    @Schema(description = "Mật khẩu", example = "Admin@123")
    private String password;
}




