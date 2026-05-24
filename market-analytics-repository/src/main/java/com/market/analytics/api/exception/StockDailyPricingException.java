package com.market.analytics.api.exception;

import com.market.analytics.exception.CoreException;
import org.springframework.http.HttpStatus;

public class StockDailyPricingException extends CoreException {

    public StockDailyPricingException() {
        super(HttpStatus.NOT_FOUND, "", "Stock daily pricing was not found");
    }

}
