package com.attendance.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeContract {

    // Mã lỗi nội bộ.
    int getCode();

    // Thông điệp mặc định của lỗi.
    String getMessage();

    // HTTP status tương ứng.
    HttpStatus getStatus();
}



