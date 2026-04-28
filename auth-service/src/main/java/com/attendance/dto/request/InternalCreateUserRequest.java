package com.attendance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record InternalCreateUserRequest(
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(max = 120, message = "Tên đăng nhập tối đa 120 ký tự")
    String username,

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    String password,

    @Email(message = "Email không hợp lệ")
    @Size(max = 190, message = "Email tối đa 190 ký tự")
    String email,

    boolean enabled,

    @NotEmpty(message = "Phải có ít nhất một vai trò")
    Set<String> roles
) {}
