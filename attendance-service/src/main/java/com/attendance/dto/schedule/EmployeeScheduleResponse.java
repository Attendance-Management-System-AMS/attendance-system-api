package com.attendance.dto.schedule;

import java.time.LocalDate;

public record EmployeeScheduleResponse(
        Long id,
        Long employeeId,
        String employeeName,
        Long shiftId,
        String shiftName,
        Integer dayOfWeek,
        Boolean isActive,
        LocalDate effectiveFrom
) {
}
