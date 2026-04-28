package com.attendance.dto.response;

public record PositionResponse(
    Long id,
    String name,
    Long departmentId,
    String departmentName,
    String level,
    Long parentPositionId,
    String parentPositionName
) {}

