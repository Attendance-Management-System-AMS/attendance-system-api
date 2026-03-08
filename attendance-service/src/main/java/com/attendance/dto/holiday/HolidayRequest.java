package com.attendance.dto.holiday;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HolidayRequest(
        @NotBlank(message = "Tên ngày nghỉ là bắt buộc")
        @Size(max = 200, message = "Tên ngày nghỉ tối đa 200 ký tự")
        String holidayName,

        @NotNull(message = "Ngày bắt đầu là bắt buộc")
        LocalDate fromDate,

        @NotNull(message = "Ngày kết thúc là bắt buộc")
        LocalDate toDate,

        @NotNull(message = "Trạng thái hưởng lương là bắt buộc")
        Boolean isPaid
) {
}
