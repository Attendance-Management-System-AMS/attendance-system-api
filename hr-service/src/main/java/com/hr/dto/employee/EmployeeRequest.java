package com.hr.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "Mã nhân viên là bắt buộc")
        @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự")
        String employeeCode,

        @NotBlank(message = "Họ và tên là bắt buộc")
        @Size(max = 180, message = "Họ và tên tối đa 180 ký tự")
        String fullName,

        @Size(max = 20, message = "Giới tính tối đa 20 ký tự")
        String gender,

        @Email(message = "Email không hợp lệ")
        @Size(max = 190, message = "Email tối đa 190 ký tự")
        String email,

        Long departmentId,
        Long positionId,
        Long managerId,

        @Size(max = 40, message = "Trạng thái tối đa 40 ký tự")
        String status,

        @Size(max = 512, message = "Mã sinh trắc học tối đa 512 ký tự")
        String biometricHash,

        LocalDate joinDate
) {
}
