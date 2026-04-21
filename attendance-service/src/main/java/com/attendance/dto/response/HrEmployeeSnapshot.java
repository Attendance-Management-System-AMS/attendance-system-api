package com.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HrEmployeeSnapshot(
        Long id,
        String employeeCode,
        String fullName,
        String departmentName,
        String positionName
) {}

