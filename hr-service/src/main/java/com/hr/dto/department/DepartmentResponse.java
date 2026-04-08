package com.hr.dto.department;

import java.time.LocalDateTime;

public record DepartmentResponse(
                Long id,
                String name,
                String description,
                String status,
                LocalDateTime createdAt) {
}
