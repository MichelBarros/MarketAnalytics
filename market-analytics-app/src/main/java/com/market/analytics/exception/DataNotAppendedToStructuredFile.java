package com.market.analytics.exception;

import org.springframework.http.HttpStatus;

public class DataNotAppendedToStructuredFile extends CoreException {
    public DataNotAppendedToStructuredFile() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "", "Data was not appended to structured file.");
    }
}
