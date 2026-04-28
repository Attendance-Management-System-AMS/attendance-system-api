package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
    @NotBlank(message = "Tên phòng ban là bắt buộc")
    @Size(max = 120, message = "Tên phòng ban tối đa 120 ký tự")
    String name,

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    String description,

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Trạng thái phòng ban không hợp lệ")
    String status
) {}

