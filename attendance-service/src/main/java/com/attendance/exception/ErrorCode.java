package com.attendance.exception;

import com.common.exception.ErrorCodeContract;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements ErrorCodeContract {

    UNCATEGORIZED_ERROR(1999, "Lỗi chưa xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED(1001, "Xác thực dữ liệu thất bại", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1002, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1003, "Không tìm thấy dữ liệu", HttpStatus.NOT_FOUND),
    FORBIDDEN(1004, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1005, "Chưa đăng nhập", HttpStatus.UNAUTHORIZED),

    SHIFT_NOT_FOUND(2004, "Không tìm thấy ca làm", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(2005, "Không tìm thấy lịch làm", HttpStatus.NOT_FOUND),
    HOLIDAY_NOT_FOUND(2006, "Không tìm thấy ngày nghỉ", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus status;

    // Khởi tạo mã lỗi, thông điệp và HTTP status tương ứng.
    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    // Lấy mã lỗi nội bộ.
    public int getCode() {
        return code;
    }

    // Lấy thông điệp lỗi.
    public String getMessage() {
        return message;
    }

    // Lấy HTTP status đi kèm.
    public HttpStatus getStatus() {
        return status;
    }
}
