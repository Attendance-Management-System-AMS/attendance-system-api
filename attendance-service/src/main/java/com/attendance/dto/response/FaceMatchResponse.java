package com.attendance.dto.response;

public record FaceMatchResponse(
    Long employeeId,
    Double similarity,
    String employeeCode,
    String fullName
) {
    public FaceMatchResponse(Long employeeId, Double similarity) {
        this(employeeId, similarity, null, null);
    }
}

