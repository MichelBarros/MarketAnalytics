package com.market.analytics.exception;

import org.springframework.http.HttpStatus;

public class DataStructuredFileNotCreated extends CoreException {

    public DataStructuredFileNotCreated() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "", "Data structured file was not created.");
    }

}
