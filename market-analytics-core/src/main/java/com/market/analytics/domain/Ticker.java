package com.market.analytics.domain;

import java.math.BigDecimal;
import java.util.Date;

public record Ticker(
        int id,
        String ticker,
        String name,
        String market,
        String locale,
        String primaryExchange,
        String type,
        Boolean active,
        String currencyName,
        int cik,
        String compositeFigi,
        String shareClassFigi,
        BigDecimal marketCap,
        String description,
        String sicCode,
        String sicDescription,
        String tickerRoot,
        String homepageUrl,
        int totalEmployees,
        Date listDate,
        String logoUrl,
        String iconUrl,
        Long shareClassSharesOutstanding,
        Long weightedSharesOutstanding,
        int roundLot
) {
}
