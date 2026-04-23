package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeScheduleRequest(
        @Schema(description = "ID nhân viên", example = "12")
        @NotNull(message = "Mã nhân viên là bắt buộc")
        Long employeeId,

        @Schema(description = "ID ca làm", example = "3")
        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId,

        @Schema(description = "Thứ trong tuần (1-7, 1=Thứ 2, 7=Chủ nhật)", example = "1")
        @NotNull(message = "Thứ trong tuần là bắt buộc")
        @Min(value = 1, message = "Thứ trong tuần phải từ 1 đến 7")
        @Max(value = 7, message = "Thứ trong tuần phải từ 1 đến 7")
        Integer dayOfWeek,

        @Schema(description = "Lịch còn hiệu lực hay không", example = "true")
        @NotNull(message = "Trạng thái hoạt động là bắt buộc")
        Boolean isActive,

        @Schema(description = "Ngày bắt đầu áp dụng, chỉ cho phép từ hôm nay trở đi", example = "2026-06-01")
        @NotNull(message = "Ngày hiệu lực là bắt buộc")
        LocalDate effectiveFrom,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}


