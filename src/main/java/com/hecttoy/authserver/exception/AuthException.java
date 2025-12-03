package com.hecttoy.authserver.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private Integer code;
    private String message;

    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AuthException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }
}
