package com.hr.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequest(
        @Schema(description = "Mã nhân viên", example = "EMP001")
        @NotBlank(message = "Mã nhân viên là bắt buộc")
        @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự")
        String employeeCode,

        @Schema(description = "Họ tên nhân viên", example = "Nguyễn Văn A")
        @NotBlank(message = "Họ và tên là bắt buộc")
        @Size(max = 180, message = "Họ và tên tối đa 180 ký tự")
        String fullName,

        @Schema(description = "Giới tính", example = "MALE")
        @Size(max = 20, message = "Giới tính tối đa 20 ký tự")
        String gender,

        @Schema(description = "Email công việc", example = "nguyenvana@company.com")
        @Email(message = "Email không hợp lệ")
        @Size(max = 190, message = "Email tối đa 190 ký tự")
        String email,

        @Schema(description = "ID phòng ban", example = "2")
        Long departmentId,
        @Schema(description = "ID chức vụ", example = "5")
        Long positionId,
        @Schema(description = "ID quản lý trực tiếp", example = "1")
        Long managerId,

        @Schema(description = "Trạng thái làm việc", example = "ACTIVE")
        @Size(max = 40, message = "Trạng thái tối đa 40 ký tự")
        String status,

        @Schema(description = "Mã hash sinh trắc học", example = "sha256:7f1a3c4d...")
        @Size(max = 512, message = "Mã sinh trắc học tối đa 512 ký tự")
        String biometricHash,

        @Schema(description = "Ngày vào làm", example = "2024-08-01")
        LocalDate joinDate
) {
}
