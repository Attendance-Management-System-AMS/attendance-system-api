package com.attendance.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCodeContract {

    int getCode();

    String getMessage();

    HttpStatus getStatus();
}
