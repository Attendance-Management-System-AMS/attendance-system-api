package com.hr.dto.shift;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ShiftResponse(
        Long id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        LocalTime breakStart,
        LocalTime breakEnd,
        Integer gracePeriod,
        LocalDateTime createdAt
) {
}
