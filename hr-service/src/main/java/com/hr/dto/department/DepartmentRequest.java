package com.hr.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @Schema(description = "Tên phòng ban", example = "Phòng Công nghệ")
        @NotBlank(message = "Tên phòng ban là bắt buộc")
        @Size(max = 120, message = "Tên phòng ban tối đa 120 ký tự")
        String name,

        @Schema(description = "Mô tả phòng ban", example = "Quản lý hệ thống phần mềm và hạ tầng")
        @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
        String description
) {
}
