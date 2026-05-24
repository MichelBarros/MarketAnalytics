package com.market.analytics.api;

import com.market.analytics.api.exception.StockDailyPricingException;
import com.market.analytics.api.exception.TickerNotFoundException;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.TickerDetails;
import com.market.analytics.domain.TickerSymbols;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author Michel Barros
 */
@Slf4j
@Repository
public class StockDataRepositoryImpl implements StockDataRepository {

    private static final String API_KEY_PARAM = "apiKey";
    private static final String LIMIT_PARAM = "limit";
    private static final String CURSOR_PARAM = "cursor";
    private static final String ADJUSTED_PARAM = "adjusted";
    private static final String INCLUDE_OTC_PARAM = "include_otc";

    private final WebClient webClient;
    private final MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    public StockDataRepositoryImpl(WebClient webClient,
                                   MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.webClient = webClient;
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    @Override
    public Mono<TickerSymbols> getAllTickers(Optional<String> nextUrl) {
        Optional<String> cursor = Optional.empty();
        if (nextUrl.isPresent()) {
            UriComponents components = UriComponentsBuilder.fromUri(URI.create(nextUrl.get())).build();
            var queryParam = components.getQueryParams().getFirst("cursor");
            cursor = Optional.ofNullable(queryParam);
        }
        log.info("Getting list of ticker symbols");
        Optional<String> finalCursor = cursor;
        return this.webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .scheme("https")
                                .host(this.marketAnalyticsConfigProperties.getApis().getBulkData().getBaseUrl())
                                .path(this.marketAnalyticsConfigProperties.getApis().getBulkData().getAllTickers())
                                .queryParam(LIMIT_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getLimit())
                                .queryParam(API_KEY_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getApiKey())
                                .queryParamIfPresent(CURSOR_PARAM, finalCursor)
                                .build()
                )
                .retrieve()
                .bodyToMono(TickerSymbols.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .maxBackoff(Duration.ofSeconds(10))
                                .filter(this::isRetryableError)
                )
                .map(ts -> new TickerSymbols(ts.results(), ts.status(), ts.requestId(), ts.count(), ts.nextUrl()))
                .delayElement(Duration.ofSeconds(this.marketAnalyticsConfigProperties.getApis().getBulkData().getDelay()))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error(ex.toString());
                    throw new TickerNotFoundException();
                });
    }

    @Override
    public TickerDetails getTickerOverview(String ticker) {
        log.info("Getting ticker information");
        return this.webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .scheme("https")
                                .host(this.marketAnalyticsConfigProperties.getApis().getBulkData().getBaseUrl())
                                .path(this.marketAnalyticsConfigProperties.getApis().getBulkData().getTickerOverview())
                                .queryParam("apiKey", this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getApiKey())
                                .build(ticker)
                )
                .retrieve()
                .bodyToMono(TickerDetails.class)
                .delayElement(Duration.ofSeconds(
                        this.marketAnalyticsConfigProperties.getApis().getBulkData().getDelay()
                ))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error(ex.toString());
                    throw new TickerNotFoundException();
                })
                .block();
    }

    @Override
    public Mono<AssetDailyPricing> getStockDailyPricing() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate previousDay = LocalDate.now().minusDays(1);
//        String previousDayFormatted = previousDay.format(formatter);
        String previousDayFormatted = "2026-05-15";

        log.info("Getting stock grouped daily (bars) pricing");
        return this.webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .scheme("https")
                                .host(this.marketAnalyticsConfigProperties.getApis().getBulkData().getBaseUrl())
                                .path(this.marketAnalyticsConfigProperties.getApis().getBulkData().getStocksDaily())
                                .queryParam(ADJUSTED_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getAdjusted())
                                .queryParam(INCLUDE_OTC_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getIncludeOtc())
                                .queryParam(API_KEY_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getApiKey())
                                .build(previousDayFormatted)
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AssetDailyPricing.class)
                .map(assetDailyPricing -> new AssetDailyPricing(
                        assetDailyPricing.requestId(),
                        assetDailyPricing.queryCount(),
                        assetDailyPricing.resultsCount(),
                        assetDailyPricing.adjusted(),
                        assetDailyPricing.status(),
                        assetDailyPricing.count(),
                        assetDailyPricing.results()
                ))
                .delayElement(Duration.ofSeconds(this.marketAnalyticsConfigProperties.getApis().getBulkData().getDelay()))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error(ex.toString());
                    throw new StockDailyPricingException();
                });
    }

    private boolean isRetryableError(Throwable t) {
        return t instanceof IOException ||
                t instanceof WebClientRequestException;
    }

}
