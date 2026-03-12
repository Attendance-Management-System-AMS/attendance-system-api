package com.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeContract {

    int getCode();

    String getMessage();

    HttpStatus getStatus();
}
