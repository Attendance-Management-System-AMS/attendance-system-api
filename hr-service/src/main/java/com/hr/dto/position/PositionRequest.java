package com.hr.dto.position;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record PositionRequest(
        @Schema(description = "Tên chức vụ", example = "Trưởng nhóm Kỹ thuật")
        @NotBlank(message = "Tên chức vụ là bắt buộc")
        @Size(max = 120, message = "Tên chức vụ tối đa 120 ký tự")
        String name,

        @Schema(description = "ID phòng ban", example = "2")
        @NotNull(message = "Mã phòng ban là bắt buộc")
        Long departmentId,

        @Schema(description = "Cấp bậc chức vụ", example = "3")
        @NotNull(message = "Cấp bậc là bắt buộc")
        @Min(value = 1, message = "Cấp bậc phải lớn hơn hoặc bằng 1")
        Integer level
) {
}
