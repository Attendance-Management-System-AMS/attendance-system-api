package com.hr.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * Cùng cách đo với face-api.js {@code faceapi.euclideanDistance}: sqrt(sum (a_i - b_i)^2).
 */
public final class FaceEmbeddingUtils {

    public static final int FACE_DESCRIPTOR_LENGTH = 128;

    // Không cho khởi tạo vì đây là lớp tiện ích.
    private FaceEmbeddingUtils() {
    }

    // Tính khoảng cách Euclidean giữa hai vector descriptor.
    public static double euclideanDistance(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Độ dài vector không khớp");
        }
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    // Chuyển descriptor từ List sang mảng primitive double.
    public static double[] toDoubleArray(List<Double> descriptor) {
        return descriptor.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // Đọc descriptor từ JSON sang mảng double.
    public static double[] fromJson(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.readValue(json, double[].class);
    }
}
