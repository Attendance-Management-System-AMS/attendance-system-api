package com.attendance.common.error;

public class BaseAppException extends RuntimeException {

    private final ErrorCodeContract errorCode;
    private final Object data;

    public BaseAppException(ErrorCodeContract errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    public BaseAppException(ErrorCodeContract errorCode, String message) {
        this(errorCode, message, null);
    }

    public BaseAppException(ErrorCodeContract errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public ErrorCodeContract getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }
}
