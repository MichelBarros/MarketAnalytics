package com.market.analytics.api;

import com.market.analytics.api.exception.CryptoDailyPricingException;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.domain.AssetDailyPricing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Repository
public class CryptoDataRepositoryImpl implements CryptoDataRepository {

    private static final String API_KEY_PARAM = "apiKey";
    private static final String ADJUSTED_PARAM = "adjusted";

    private final WebClient webClient;
    private final MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    public CryptoDataRepositoryImpl(WebClient webClient,
                                    MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.webClient = webClient;
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    @Override
    public Mono<AssetDailyPricing> getCryptoDailyPricing() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate previousDay = LocalDate.now().minusDays(1);
//        String previousDayFormatted = previousDay.format(formatter);
        String previousDayFormatted = "2026-05-15";

        log.info("Getting crypto grouped daily (bars) pricing");
        return this.webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .scheme("https")
                                .host(this.marketAnalyticsConfigProperties.getApis().getBulkData().getBaseUrl())
                                .path(this.marketAnalyticsConfigProperties.getApis().getBulkData().getCryptoDaily())
                                .queryParam(ADJUSTED_PARAM, this.marketAnalyticsConfigProperties.getApis().getBulkData().getQueryParams().getAdjusted())
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
                    throw new CryptoDailyPricingException();
                });
    }

}
