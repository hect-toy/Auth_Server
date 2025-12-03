package com.hecttoy.authserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private String status;
    private Long timestamp;
    private T data;
    private String path;

    public StandardResponse(Integer code, String message, String status, T data) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> StandardResponse<T> success(Integer code, String message, T data) {
        return StandardResponse.<T>builder()
            .code(code)
            .message(message)
            .status("SUCCESS")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> StandardResponse<T> error(Integer code, String message) {
        return StandardResponse.<T>builder()
            .code(code)
            .message(message)
            .status("ERROR")
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
