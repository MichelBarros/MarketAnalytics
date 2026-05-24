package com.market.analytics.exception;

import org.springframework.http.HttpStatus;

public class TaskletExecutionException extends CoreException {

    public TaskletExecutionException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "", message);
    }
}
