package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateEmployeeRequest(
    @Schema(example = "Nguyen Van A")
    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(max = 180, message = "Họ tên tối đa 180 ký tự")
    String fullName,

    @Schema(example = "MALE")
    @Size(max = 20, message = "Giới tính tối đa 20 ký tự")
    String gender,

    @Schema(example = "nguyenvana@company.com")
    @Email(message = "Email không hợp lệ")
    @Size(max = 190, message = "Email tối đa 190 ký tự")
    String email,

    @Schema(example = "3")
    Long departmentId,
    @Schema(example = "4")
    Long positionId,
    @Schema(example = "2")
    Long managerId,

    @Schema(example = "ACTIVE")
    @Pattern(regexp = "ACTIVE|INACTIVE|ON_LEAVE|TERMINATED", message = "Trạng thái nhân viên không hợp lệ")
    String status,

    @Schema(example = "sha256:optional-biometric-hash")
    @Size(max = 512, message = "Biometric hash tối đa 512 ký tự")
    String biometricHash,

    @Schema(example = "2026-04-22")
    LocalDate joinDate
) {}
