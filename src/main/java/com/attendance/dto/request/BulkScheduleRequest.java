package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

        @Schema(description = "Danh sách các thứ trong tuần (2-8, với 8 là Chủ nhật)", example = "[2, 3, 4, 5, 6]")
        @NotEmpty(message = "Danh sách ngày trong tuần không được để trống")
        List<Integer> daysOfWeek,

        @Schema(description = "Ngày bắt đầu áp dụng", example = "2026-04-01")
        @NotNull(message = "Ngày hiệu lực là bắt buộc")
        LocalDate effectiveFrom,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}


