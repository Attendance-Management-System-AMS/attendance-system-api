package com.hr.exception;

import com.common.dto.ApiResponse;
import com.common.exception.AppException;
import com.common.exception.ErrorCodeContract;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCodeContract errorCode = ex.getErrorCode();
        String message = ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage();

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getCode(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();

        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_ERROR.getCode(), ErrorCode.UNCATEGORIZED_ERROR.getMessage()));
    }
}
