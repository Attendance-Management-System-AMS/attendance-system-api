package com.hr.dto.position;

public record PositionResponse(
        Long id,
        String name,
        Long departmentId,
        String departmentName,
        Integer level
) {
}
