package com.hr.dto.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LeaveRequestRecord(
        @Schema(description = "ID nhân viên", example = "12")
        @NotNull(message = "Mã nhân viên là bắt buộc")
        Long employeeId,

        @Schema(description = "Loại nghỉ", example = "ANNUAL")
        @NotBlank(message = "Loại nghỉ là bắt buộc")
        String leaveType,

        @Schema(description = "Ngày bắt đầu", example = "2026-03-20")
        @NotNull(message = "Ngày bắt đầu là bắt buộc")
        LocalDate fromDate,

        @Schema(description = "Ngày kết thúc", example = "2026-03-22")
        @NotNull(message = "Ngày kết thúc là bắt buộc")
        LocalDate toDate,

        @Schema(description = "Lý do nghỉ", example = "Nghỉ phép năm cùng gia đình")
        String reason
) {
}
