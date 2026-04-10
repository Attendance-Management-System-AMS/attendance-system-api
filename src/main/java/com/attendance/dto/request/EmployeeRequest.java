package com.attendance.dto.request;

import java.time.LocalDate;

public record EmployeeRequest(
    String employeeCode,
    String fullName,
    String gender,
    String email,
    Long departmentId,
    Long positionId,
    Long managerId,
    String status,
    String biometricHash,
    LocalDate joinDate
) {}

