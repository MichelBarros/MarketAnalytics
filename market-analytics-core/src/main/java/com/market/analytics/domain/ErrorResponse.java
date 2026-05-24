package com.market.analytics.domain;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        HttpStatus statusCode,
        String code,
        String message
) {
}
