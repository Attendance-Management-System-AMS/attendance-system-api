package com.attendance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequest(
    @NotBlank(message = "Mã nhân viên là bắt buộc")
    @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự")
    String employeeCode,

    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(max = 180, message = "Họ tên tối đa 180 ký tự")
    String fullName,

    @Size(max = 20, message = "Giới tính tối đa 20 ký tự")
    String gender,

    @Email(message = "Email không hợp lệ")
    @Size(max = 190, message = "Email tối đa 190 ký tự")
    String email,

    Long departmentId,
    Long positionId,
    Long managerId,

    @Pattern(regexp = "ACTIVE|INACTIVE|ON_LEAVE|TERMINATED", message = "Trạng thái nhân viên không hợp lệ")
    String status,

    @Size(max = 512, message = "Biometric hash tối đa 512 ký tự")
    String biometricHash,

    LocalDate joinDate
) {}

