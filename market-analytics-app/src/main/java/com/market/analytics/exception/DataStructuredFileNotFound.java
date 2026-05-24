package com.market.analytics.exception;

import org.springframework.http.HttpStatus;

public class DataStructuredFileNotFound extends CoreException {
    public DataStructuredFileNotFound() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "", "Data structured file was not found.");
    }
}
