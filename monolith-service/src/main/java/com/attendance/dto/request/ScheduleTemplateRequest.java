package com.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ScheduleTemplateRequest(
        @Schema(description = "Tên mẫu lịch làm việc", example = "Lịch hành chính chuẩn")
        @NotBlank(message = "Tên mẫu lịch là bắt buộc")
        String name,

        @Schema(description = "Mô tả chi tiết mẫu lịch", example = "Thứ 2 đến thứ 6 làm 8:00 - 17:00, Thứ 7 làm 8:00 - 12:00")
        String description,

        @Schema(description = "Danh sách các ngày và ca làm gán theo mẫu")
        @NotEmpty(message = "Mẫu lịch phải có ít nhất một ngày làm việc")
        List<ScheduleTemplateItemRequest> items
) {
}


