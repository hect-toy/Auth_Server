package com.hecttoy.authserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardRequest<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private Long timestamp;
    private T data;

    public StandardRequest(T data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}
