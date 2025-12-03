package com.hecttoy.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private String timestamp;

    public static <T> StandardResponse<T> ok(T data, String message) {
        return StandardResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(200)
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();
    }

    public static <T> StandardResponse<T> ok(T data) {
        return ok(data, "Success");
    }

    public static <T> StandardResponse<T> created(T data, String message) {
        return StandardResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(201)
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();
    }

    public static <T> StandardResponse<T> error(String message, int statusCode) {
        return StandardResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .timestamp(String.valueOf(System.currentTimeMillis()))
            .build();
    }
}
