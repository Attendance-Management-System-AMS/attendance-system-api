package com.common.exception;

public class AppException extends RuntimeException {

    private final ErrorCodeContract errorCode;

    // Tạo exception theo mã lỗi mặc định của hệ thống.
    public AppException(ErrorCodeContract errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // Tạo exception với thông điệp tuỳ chỉnh.
    public AppException(ErrorCodeContract errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // Lấy mã lỗi gốc.
    public ErrorCodeContract getErrorCode() {
        return errorCode;
    }
}
