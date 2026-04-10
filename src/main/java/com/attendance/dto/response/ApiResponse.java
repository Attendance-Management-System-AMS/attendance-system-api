package com.attendance.dto.response;

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

    // Tạo response thành công mặc định.
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

    // Tạo response lỗi chuẩn hóa.
    public static <T> ApiResponse<T> error(int code, String message) {
        return error(code, message, null);
    }

    // Tạo response lỗi kèm dữ liệu bổ sung.
    public static <T> ApiResponse<T> error(int code, String message, T result) {
        return new ApiResponse<>(false, code, message, result, Instant.now());
    }

    // Kiểm tra request có thành công hay không.
    public boolean isSuccess() {
        return success;
    }

    // Lấy mã trạng thái nội bộ.
    public int getCode() {
        return code;
    }

    // Lấy thông điệp phản hồi.
    public String getMessage() {
        return message;
    }

    // Lấy dữ liệu trả về.
    public T getResult() {
        return result;
    }

    // Lấy thời điểm tạo response.
    public Instant getTimestamp() {
        return timestamp;
    }
}



