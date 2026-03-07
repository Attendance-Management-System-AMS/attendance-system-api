package com.hr.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Tên phòng ban là bắt buộc")
        @Size(max = 120, message = "Tên phòng ban tối đa 120 ký tự")
        String name,

        @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
        String description
) {
}
