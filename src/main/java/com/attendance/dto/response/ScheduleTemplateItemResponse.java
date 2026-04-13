package com.attendance.dto.response;

public record ScheduleTemplateItemResponse(
        Long id,
        Integer dayOfWeek,
        ShiftResponse shift
) {
}


