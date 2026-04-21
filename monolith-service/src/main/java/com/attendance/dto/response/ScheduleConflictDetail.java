package com.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ScheduleConflictDetail(
        @Schema(description = "ID nhân viên bị xung đột")
        Long employeeId,
        
        @Schema(description = "Thứ trong tuần (2-8)")
        Integer dayOfWeek,
        
        @Schema(description = "Tên ca làm định gán")
        String newShiftName,
        
        @Schema(description = "Tên ca làm hiện tại đang bị trùng")
        String existingShiftName
) {
}


