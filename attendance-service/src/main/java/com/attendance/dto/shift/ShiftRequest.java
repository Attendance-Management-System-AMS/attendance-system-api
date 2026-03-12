package com.attendance.dto.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record ShiftRequest(
        @Schema(description = "Tên ca làm", example = "Ca sáng")
        @NotBlank(message = "Tên ca làm là bắt buộc")
        @Size(max = 120, message = "Tên ca làm tối đa 120 ký tự")
        String name,

        @Schema(description = "Giờ bắt đầu", example = "08:00:00")
        @NotNull(message = "Giờ bắt đầu là bắt buộc")
        LocalTime startTime,

        @Schema(description = "Giờ kết thúc", example = "17:00:00")
        @NotNull(message = "Giờ kết thúc là bắt buộc")
        LocalTime endTime,

        @Schema(description = "Giờ bắt đầu nghỉ giữa ca", example = "12:00:00")
        LocalTime breakStart,

        @Schema(description = "Giờ kết thúc nghỉ giữa ca", example = "13:00:00")
        LocalTime breakEnd,

        @Schema(description = "Số phút cho phép đi muộn", example = "10")
        @Min(value = 0, message = "Thời gian cho phép đi muộn phải lớn hơn hoặc bằng 0")
        Integer gracePeriod
) {
}
