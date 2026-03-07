package com.hr.dto.shift;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record ShiftRequest(
        @NotBlank(message = "Tên ca làm là bắt buộc")
        @Size(max = 120, message = "Tên ca làm tối đa 120 ký tự")
        String name,

        @NotNull(message = "Giờ bắt đầu là bắt buộc")
        LocalTime startTime,

        @NotNull(message = "Giờ kết thúc là bắt buộc")
        LocalTime endTime,

        LocalTime breakStart,
        LocalTime breakEnd,

        @Min(value = 0, message = "Thời gian cho phép đi muộn phải lớn hơn hoặc bằng 0")
        Integer gracePeriod
) {
}
