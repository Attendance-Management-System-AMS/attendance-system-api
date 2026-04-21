package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ApplyTemplateRequest(
        @Schema(description = "Danh sách ID nhân viên", example = "[1, 2, 3]")
        @NotEmpty(message = "Danh sách nhân viên không được để trống")
        List<Long> employeeIds,

        @Schema(description = "ID của mẫu lịch", example = "1")
        @NotNull(message = "Mã mẫu lịch là bắt buộc")
        Long templateId,

        @Schema(description = "Ngày bắt đầu áp dụng", example = "2026-04-01")
        @NotNull(message = "Ngày hiệu lực là bắt buộc")
        LocalDate effectiveFrom,

        @Schema(description = "Có bắt buộc gán ca hay không (nếu true sẽ ghi đè ca cũ bị trùng)", example = "false")
        Boolean force
) {
}


