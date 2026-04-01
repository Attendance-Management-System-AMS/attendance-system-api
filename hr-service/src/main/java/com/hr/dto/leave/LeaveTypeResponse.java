package com.hr.dto.leave;

import io.swagger.v3.oas.annotations.media.Schema;

public record LeaveTypeResponse(
        @Schema(description = "ID loại nghỉ")
        Long id,

        @Schema(description = "Mã loại nghỉ", example = "AL")
        String code,

        @Schema(description = "Tên loại nghỉ", example = "Nghỉ phép năm")
        String name,

        @Schema(description = "Có hưởng lương", example = "true")
        Boolean isPaid,

        @Schema(description = "Trừ phép năm", example = "true")
        Boolean deductAnnualLeave,

        @Schema(description = "Bảo hiểm trả", example = "false")
        Boolean insuranceCovers,

        @Schema(description = "Đang hoạt động", example = "true")
        Boolean isActive,

        @Schema(description = "Mô tả")
        String description
) {
}
