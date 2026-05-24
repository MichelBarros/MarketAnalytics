package com.market.analytics.domain;

public enum QueryResponseStatus {

    NOT_FOUND("NOT_FOUND"),
    OK("OK");

    public final String message;

    QueryResponseStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
