package com.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record OvertimeRequestRecord(
        Long employeeId,

        @NotNull(message = "Ngày tăng ca là bắt buộc")
        LocalDate workDate,

        @NotNull(message = "Giờ bắt đầu tăng ca là bắt buộc")
        LocalTime startTime,

        @NotNull(message = "Giờ kết thúc tăng ca là bắt buộc")
        LocalTime endTime,

        @Size(max = 500, message = "Lý do tối đa 500 ký tự")
        String reason
) {
}
