package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScheduleTemplateItemRequest(
        @Schema(description = "Thứ trong tuần (1-7, 1=Thứ 2, 7=Chủ nhật)", example = "1")
        @NotNull(message = "Thứ trong tuần là bắt buộc")
        @Min(value = 1, message = "Thứ từ 1-7")
        @Max(value = 7, message = "Thứ từ 1-7")
        Integer dayOfWeek,

        @Schema(description = "ID ca làm", example = "1")
        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId
) {
}


