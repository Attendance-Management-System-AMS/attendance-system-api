package com.attendance.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        int code,
        String message,
        T result,
        Instant timestamp) {

    public static <T> ApiResponse<T> success(T result) {
        return success(200, "Success", result);
    }

    public static <T> ApiResponse<T> success(String message, T result) {
        return success(200, message, result);
    }

    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return new ApiResponse<>(true, code, message, result, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return error(code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T result) {
        return new ApiResponse<>(false, code, message, result, Instant.now());
    }
}
