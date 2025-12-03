package com.hecttoy.authserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private String status;
    private Long timestamp;
    private String path;
    private String exception;
    private Map<String, String> validationErrors;

    public static ErrorResponse of(Integer code, String message, String path) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .status("ERROR")
            .timestamp(System.currentTimeMillis())
            .path(path)
            .build();
    }

    public static ErrorResponse ofException(Integer code, String message, String exception, String path) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .status("ERROR")
            .exception(exception)
            .timestamp(System.currentTimeMillis())
            .path(path)
            .build();
    }
}
