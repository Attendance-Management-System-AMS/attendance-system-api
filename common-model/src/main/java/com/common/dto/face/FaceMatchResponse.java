package com.common.dto.face;

/**
 * Kết quả so khớp descriptor với nhân viên đã đăng ký (khoảng cách Euclidean, càng nhỏ càng giống).
 */
public record FaceMatchResponse(Long employeeId, double distance) {
}
