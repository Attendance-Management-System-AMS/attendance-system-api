package com.attendance.dto.template;

import com.attendance.dto.shift.ShiftResponse;

public record ScheduleTemplateItemResponse(
        Long id,
        Integer dayOfWeek,
        ShiftResponse shift
) {
}
