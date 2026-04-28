package com.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
    Long id,
    Long userId,
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
    boolean faceRegistered,
    LocalDate joinDate,
    LocalDateTime createdAt
) {}

