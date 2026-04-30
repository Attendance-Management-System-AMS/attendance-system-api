package com.attendance.exception;

import com.attendance.common.error.BaseAppException;
import com.attendance.common.error.ErrorCodeContract;

public class AppException extends BaseAppException {

    public AppException(ErrorCodeContract errorCode) {
        super(errorCode);
    }

    public AppException(ErrorCodeContract errorCode, String message) {
        super(errorCode, message);
    }

    public AppException(ErrorCodeContract errorCode, String message, Object data) {
        super(errorCode, message, data);
    }
}



