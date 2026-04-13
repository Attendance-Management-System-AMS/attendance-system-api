package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HolidayRequest(
        @Schema(description = "Tên ngày nghỉ", example = "Giỗ Tổ Hùng Vương")
        @NotBlank(message = "Tên ngày nghỉ là bắt buộc")
        @Size(max = 200, message = "Tên ngày nghỉ tối đa 200 ký tự")
        String holidayName,

        @Schema(description = "Ngày bắt đầu nghỉ", example = "2026-04-18")
        @NotNull(message = "Ngày bắt đầu là bắt buộc")
        LocalDate fromDate,

        @Schema(description = "Ngày kết thúc nghỉ", example = "2026-04-18")
        @NotNull(message = "Ngày kết thúc là bắt buộc")
        LocalDate toDate,

        @Schema(description = "Có hưởng lương hay không", example = "true")
        @NotNull(message = "Trạng thái hưởng lương là bắt buộc")
        Boolean isPaid
) {
}


