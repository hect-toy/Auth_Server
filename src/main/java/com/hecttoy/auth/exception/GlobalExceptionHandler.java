package com.hecttoy.auth.exception;

import com.hecttoy.auth.dto.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<StandardResponse<?>> handleAuthException(AuthException ex, WebRequest request) {
        StandardResponse<?> response = StandardResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        StandardResponse<?> response = StandardResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        StandardResponse<?> response = StandardResponse.builder()
            .success(false)
            .message("Error de validación")
            .data(errors)
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<?>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        StandardResponse<?> response = StandardResponse.error("Acceso denegado", HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        StandardResponse<?> response = StandardResponse.error("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
