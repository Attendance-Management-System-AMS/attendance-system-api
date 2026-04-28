package com.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeInternalResponse(
        Long employeeId,
        Long userId,
        String fullName,
        String departmentName,
        String positionName
) {
}
