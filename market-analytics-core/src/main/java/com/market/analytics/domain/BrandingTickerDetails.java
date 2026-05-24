package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrandingTickerDetails(
        @JsonProperty("logo_url")
        String logoUrl,
        @JsonProperty("icon_url")
        String iconUrl
) {
}
