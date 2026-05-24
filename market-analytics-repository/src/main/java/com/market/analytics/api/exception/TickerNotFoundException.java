package com.market.analytics.api.exception;

import com.market.analytics.exception.CoreException;
import org.springframework.http.HttpStatus;

public class TickerNotFoundException extends CoreException {

    public TickerNotFoundException() {
        super(HttpStatus.NOT_FOUND, "", "Ticker was not found");
    }
}
