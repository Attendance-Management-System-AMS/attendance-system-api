package com.attendance.dto.response;

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


