package com.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T result;
    private final Instant timestamp;

    private ApiResponse(boolean success, int code, String message, T result, Instant timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = result;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, 200, "Success", result, Instant.now());
    }

    /**
     * Giống envelope warehouse-service: luôn kèm message rõ ràng, HTTP status do controller (200).
     */
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(true, 200, message, result, Instant.now());
    }

    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return new ApiResponse<>(true, code, message, result, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null, Instant.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
