package com.attendance.dto.template;

import java.util.List;

public record ScheduleTemplateResponse(
        Long id,
        String name,
        String description,
        List<ScheduleTemplateItemResponse> items
) {
}
