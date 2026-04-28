package com.attendance.exception;

import com.attendance.common.error.ErrorCodeContract;

public class AppException extends RuntimeException {

    private final ErrorCodeContract errorCode;
    private final Object data;

    // Tạo exception theo mã lỗi mặc định của hệ thống.
    public AppException(ErrorCodeContract errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    // Tạo exception với thông điệp tuỳ chỉnh.
    public AppException(ErrorCodeContract errorCode, String message) {
        this(errorCode, message, null);
    }

    // Tạo exception kèm theo dữ liệu bổ sung (metadata).
    public AppException(ErrorCodeContract errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    // Lấy mã lỗi gốc.
    public ErrorCodeContract getErrorCode() {
        return errorCode;
    }

    // Lấy dữ liệu đính kèm lỗi.
    public Object getData() {
        return data;
    }
}



