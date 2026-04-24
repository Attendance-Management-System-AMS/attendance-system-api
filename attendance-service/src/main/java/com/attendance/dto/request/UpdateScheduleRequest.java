package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateScheduleRequest(
        @Schema(description = "ID ca làm", example = "3")
        @NotNull(message = "Mã ca làm là bắt buộc")
        Long shiftId,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}
