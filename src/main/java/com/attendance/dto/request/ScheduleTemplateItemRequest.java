package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScheduleTemplateItemRequest(
        @Schema(description = "Thứ trong tuần (2-8, 8=CN)", example = "2")
        @NotNull(message = "Thứ trong tuần là bắt buộc")
        @Min(value = 2, message = "Thứ từ 2-8")
        @Max(value = 8, message = "Thứ từ 2-8")
        Integer dayOfWeek,

        @Schema(description = "ID ca làm", example = "1")
        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId
) {
}


