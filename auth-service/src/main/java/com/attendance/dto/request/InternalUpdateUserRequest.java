package com.attendance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalUpdateUserRequest(
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(max = 120, message = "Tên đăng nhập tối đa 120 ký tự")
    String username,

    @Email(message = "Email không hợp lệ")
    @Size(max = 190, message = "Email tối đa 190 ký tự")
    String email,

    boolean enabled
) {}
