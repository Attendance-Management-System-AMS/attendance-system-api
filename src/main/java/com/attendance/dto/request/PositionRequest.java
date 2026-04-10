package com.attendance.dto.request;

public record PositionRequest(
    String name,
    Long departmentId,
    String level,
    Long parentPositionId
) {}

