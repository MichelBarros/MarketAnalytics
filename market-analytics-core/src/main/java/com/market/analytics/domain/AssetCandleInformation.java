package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetCandleInformation(
        @JsonProperty("T")
        String ticker,
        @JsonProperty("v")
        double volume,
        @JsonProperty("vw")
        double volumeWeightedAverage,
        @JsonProperty("o")
        double open,
        @JsonProperty("c")
        double close,
        @JsonProperty("h")
        double high,
        @JsonProperty("l")
        double low,
        @JsonProperty("t")
        Long time,
        @JsonProperty("n")
        int transactionsNumber,
        @JsonProperty("otc")
        boolean otc
) implements Serializable {}
