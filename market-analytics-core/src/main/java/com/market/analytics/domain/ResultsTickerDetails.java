package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultsTickerDetails(
        String ticker,
        String name,
        String market,
        String locale,
        @JsonProperty("primary_exchange")
        String primaryExchange,
        String type,
        Boolean active,
        @JsonProperty("currency_symbol")
        String currencySymbol,
        @JsonProperty("currency_name")
        String currencyName,
        int cik,
        @JsonProperty("composite_figi")
        String compositeFigi,
        @JsonProperty("share_class_figi")
        String shareClassFigi,
        @JsonProperty("base_currency_symbol")
        String baseCurrencySymbol,
        @JsonProperty("base_currency_name")
        String baseCurrencyName,
        @JsonProperty("last_updated_utc")
        Instant lastUpdatedUtc,
        @JsonProperty("market_cap")
        BigDecimal marketCap,
        String description,
        @JsonProperty("phone_number")
        String phoneNumber,
        AddressTickerDetails address,
        @JsonProperty("sic_code")
        String sicCode,
        @JsonProperty("sic_description")
        String sicDescription,
        @JsonProperty("ticker_root")
        String tickerRoot,
        @JsonProperty("homepage_url")
        String homepageUrl,
        @JsonProperty("total_employees")
        int totalEmployees,
        @JsonProperty("list_date")
        Date listDate,
        BrandingTickerDetails branding,
        @JsonProperty("share_class_shares_outstanding")
        Long shareClassSharesOutstanding,
        @JsonProperty("weighted_shares_outstanding")
        Long weightedSharesOutstanding,
        @JsonProperty("round_lot")
        int roundLot
        ) {
}
