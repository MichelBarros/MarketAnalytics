package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author Michel Barros
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetDailyPricing(
        @JsonProperty("request_id")
        String requestId,
        int queryCount,
        int resultsCount,
        boolean adjusted,
        QueryResponseStatus status,
        int count,
        List<AssetCandleInformation> results
) implements Serializable {}
