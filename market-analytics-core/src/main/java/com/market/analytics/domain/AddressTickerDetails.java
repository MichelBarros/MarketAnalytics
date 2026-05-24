package com.market.analytics.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressTickerDetails(
        String address1,
        String city,
        String state,
        @JsonProperty("postal_code")
        String postalCode
) {
}
