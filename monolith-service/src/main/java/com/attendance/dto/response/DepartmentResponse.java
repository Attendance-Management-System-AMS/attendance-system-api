package com.attendance.dto.response;

import java.time.LocalDateTime;

public record DepartmentResponse(
    Long id,
    String name,
    String description,
    String status,
    Long employeeCount,
    LocalDateTime createdAt
) {
    // Constructor 5-arg for cases without count
    public DepartmentResponse(Long id, String name, String description, String status, LocalDateTime createdAt) {
        this(id, name, description, status, 0L, createdAt);
    }
}

