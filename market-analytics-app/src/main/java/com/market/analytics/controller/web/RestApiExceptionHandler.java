package com.market.analytics.controller.web;

import com.market.analytics.domain.ErrorResponse;
import com.market.analytics.exception.CoreException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler({CoreException.class})
    public ResponseEntity<ErrorResponse> handleExceptions(CoreException ex) {
        return new ResponseEntity(
                new ErrorResponse(
                        ex.getStatusCode(),
                        ex.getCode(),
                        ex.getMessage()
                ),
                ex.getStatusCode()
        );
    }

}
