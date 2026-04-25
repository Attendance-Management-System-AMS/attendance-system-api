package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkScheduleRequest(
        @Schema(description = "Danh sách ID nhân viên", example = "[11, 12, 13]")
        @NotEmpty(message = "Danh sách nhân viên không được để trống")
        List<Long> employeeIds,

        @Schema(description = "ID ca làm", example = "3")
        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId,

        @Schema(description = "Danh sách các thứ trong tuần (1-7, 1=Thứ 2, 7=Chủ nhật)", example = "[1, 2, 3, 4, 5]")
        @NotEmpty(message = "Danh sách ngày trong tuần không được để trống")
        List<@Min(value = 1, message = "Thứ trong tuần phải từ 1 đến 7") @Max(value = 7, message = "Thứ trong tuần phải từ 1 đến 7") Integer> daysOfWeek,

        @Schema(description = "Ngày bắt đầu áp dụng, chỉ cho phép từ hôm nay trở đi", example = "2026-06-01")
        @NotNull(message = "Ngày hiệu lực là bắt buộc")
        LocalDate effectiveFrom,

        @Schema(description = "Ngày kết thúc áp dụng, để trống nếu không có ngày kết thúc", example = "2026-06-30")
        LocalDate effectiveTo,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}


