package com.market.analytics.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CoreException extends RuntimeException {

    private final HttpStatus statusCode;

    private final String code;

    public CoreException(HttpStatus statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

}
