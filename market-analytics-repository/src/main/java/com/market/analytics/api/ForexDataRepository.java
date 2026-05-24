package com.market.analytics.api;

import com.market.analytics.domain.AssetDailyPricing;
import reactor.core.publisher.Mono;

public interface ForexDataRepository {

    Mono<AssetDailyPricing> getForexDailyPricing();

}
