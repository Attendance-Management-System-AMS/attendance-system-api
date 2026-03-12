package com.common.exception;

public class AppException extends RuntimeException {

    private final ErrorCodeContract errorCode;

    public AppException(ErrorCodeContract errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCodeContract errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodeContract getErrorCode() {
        return errorCode;
    }
}
