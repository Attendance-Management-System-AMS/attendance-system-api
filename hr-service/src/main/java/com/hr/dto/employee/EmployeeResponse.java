package com.hr.dto.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String gender,
        String email,
        Long departmentId,
        String departmentName,
        Long positionId,
        String positionName,
        Long managerId,
        String managerName,
        String status,
        String biometricHash,
        Boolean faceRegistered,
        LocalDate joinDate,
        LocalDateTime createdAt
) {
}
