package com.hecttoy.authserver.exception;

import com.hecttoy.authserver.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, WebRequest request) {
        log.error("AuthException occurred: code={}, message={}", ex.getCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .status("ERROR")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(System.currentTimeMillis())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getCode()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("ResourceNotFoundException occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(404)
            .message(ex.getMessage())
            .status("ERROR")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(System.currentTimeMillis())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex, WebRequest request) {
        log.error("TokenException occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(401)
            .message(ex.getMessage())
            .status("ERROR")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(System.currentTimeMillis())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error occurred");

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(400)
            .message("Validation failed")
            .status("ERROR")
            .validationErrors(validationErrors)
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(System.currentTimeMillis())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(500)
            .message("An unexpected error occurred")
            .status("ERROR")
            .exception(ex.getClass().getSimpleName())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(System.currentTimeMillis())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
