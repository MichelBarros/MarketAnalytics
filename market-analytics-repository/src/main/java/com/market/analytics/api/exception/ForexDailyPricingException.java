package com.market.analytics.api.exception;

import com.market.analytics.exception.CoreException;
import org.springframework.http.HttpStatus;

public class ForexDailyPricingException extends CoreException {

    public ForexDailyPricingException() {
        super(HttpStatus.NOT_FOUND, "", "Forex daily pricing was not found");
    }

}
