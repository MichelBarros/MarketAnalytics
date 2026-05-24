package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TickerSymbols(
        List<ResultsTickerDetails> results,
        QueryResponseStatus status,
        @JsonProperty("request_id")
        String requestId,
        int count,
        @JsonProperty("next_url")
        String nextUrl
) {
}
