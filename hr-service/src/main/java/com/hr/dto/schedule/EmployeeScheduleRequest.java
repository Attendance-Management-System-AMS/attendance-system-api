package com.hr.dto.schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeScheduleRequest(
        @NotNull(message = "Mã nhân viên là bắt buộc")
        Long employeeId,

        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId,

        @NotNull(message = "Thứ trong tuần là bắt buộc")
        @Min(value = 2, message = "Thứ trong tuần phải từ 2 đến 8")
        @Max(value = 8, message = "Thứ trong tuần phải từ 2 đến 8")
        Integer dayOfWeek,

        @NotNull(message = "Trạng thái hoạt động là bắt buộc")
        Boolean isActive,

        @NotNull(message = "Ngày hiệu lực là bắt buộc")
        LocalDate effectiveFrom
) {
}
