package com.market.analytics.api;

import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.TickerDetails;
import com.market.analytics.domain.TickerSymbols;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author Michel Barros
 */
public interface StockDataRepository {

    Mono<TickerSymbols> getAllTickers(Optional<String> nextUrl);

    Mono<AssetDailyPricing> getStockDailyPricing();

    TickerDetails getTickerOverview(String ticker);

}
