package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record UpdateScheduleRequest(
        @Schema(description = "ID ca làm", example = "3")
        Long shiftId,

        @Schema(description = "Ngày bắt đầu áp dụng", example = "2026-06-01")
        LocalDate effectiveFrom,

        @Schema(description = "Ngày kết thúc áp dụng, để trống nếu không có ngày kết thúc", example = "2026-06-30")
        LocalDate effectiveTo,

        @Schema(description = "Lịch còn hiệu lực hay không", example = "true")
        Boolean isActive,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}
