package com.market.analytics.api.exception;

import com.market.analytics.exception.CoreException;
import org.springframework.http.HttpStatus;

public class CryptoDailyPricingException extends CoreException {
    public CryptoDailyPricingException() {
        super(HttpStatus.NOT_FOUND, "", "Crypto daily pricing was not found");
    }
}
