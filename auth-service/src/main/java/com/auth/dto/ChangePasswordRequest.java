package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @Schema(description = "Mật khẩu hiện tại", example = "Nhanvien@123")
    private String currentPassword;

    @Schema(description = "Mật khẩu mới", example = "Nhanvien@1234")
    private String newPassword;
}
