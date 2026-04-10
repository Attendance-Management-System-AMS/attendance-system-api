package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterRequest {
    @Schema(description = "Tên đăng nhập", example = "nhanvien01")
    private String username;

    @Schema(description = "Mật khẩu", example = "Nhanvien@123")
    private String password;

    @Schema(description = "Email", example = "nhanvien01@company.com")
    private String email;
}




