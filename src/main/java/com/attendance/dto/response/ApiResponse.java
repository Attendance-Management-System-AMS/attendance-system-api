package com.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T result;
    private final Instant timestamp;

    // Tạo response thành công mặc định.
    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Giống envelope warehouse-service: luôn kèm message rõ ràng, HTTP status do controller (200).
     */
    public static <T> ApiResponse<T> success(String message, T result) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code)
                .message(message)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    // Tạo response lỗi chuẩn hóa.
    public static <T> ApiResponse<T> error(int code, String message) {
        return error(code, message, null);
    }

    // Tạo response lỗi kèm dữ liệu bổ sung.
    public static <T> ApiResponse<T> error(int code, String message, T result) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }
}



