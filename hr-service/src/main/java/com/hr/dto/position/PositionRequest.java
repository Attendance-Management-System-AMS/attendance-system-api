package com.hr.dto.position;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PositionRequest(
        @NotBlank(message = "Tên chức vụ là bắt buộc")
        @Size(max = 120, message = "Tên chức vụ tối đa 120 ký tự")
        String name,

        @NotNull(message = "Mã phòng ban là bắt buộc")
        Long departmentId,

        @NotNull(message = "Cấp bậc là bắt buộc")
        @Min(value = 1, message = "Cấp bậc phải lớn hơn hoặc bằng 1")
        Integer level
) {
}
