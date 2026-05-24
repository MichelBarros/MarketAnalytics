package com.market.analytics.domain;

public record AssetDataParams(
        String ticker,
        Temporality temporality,
        int pageNumber,
        int pageSize
) {
}
