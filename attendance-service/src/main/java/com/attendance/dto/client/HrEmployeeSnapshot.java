package com.attendance.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HrEmployeeSnapshot(
        String fullName,
        String employeeCode,
        String departmentName,
        String positionName
) {}
