package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TickerDetails(
        @JsonProperty("request_id")
        String requestId,
        ResultsTickerDetails results,
        QueryResponseStatus status
) {
}
