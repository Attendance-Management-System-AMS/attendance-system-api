package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PositionRequest(
    @NotBlank(message = "Tên chức vụ là bắt buộc")
    @Size(max = 120, message = "Tên chức vụ tối đa 120 ký tự")
    String name,

    @NotNull(message = "Phòng ban là bắt buộc")
    Long departmentId,

    @NotBlank(message = "Level chức vụ là bắt buộc")
    @Pattern(regexp = "\\d+|LEVEL_\\d+", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Level chức vụ phải có dạng số hoặc LEVEL_<số>")
    String level,

    Long parentPositionId
) {}

