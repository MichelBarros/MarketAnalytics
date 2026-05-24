package com.market.analytics.service;

import com.market.analytics.api.StockDataRepository;
import com.market.analytics.db.AssetsRepository;
import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.Ticker;
import com.market.analytics.domain.TickerDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

/**
 * @author Michel Barros
 */
@Slf4j
@Service
public class StockDataServiceImpl implements StockDataService {

    private final StockDataRepository stockDataRepository;
    private final AssetsRepository tickerDetailsRepository;

    public StockDataServiceImpl(StockDataRepository stockDataRepository, AssetsRepository tickerDetailsRepository) {
        this.stockDataRepository = stockDataRepository;
        this.tickerDetailsRepository = tickerDetailsRepository;
    }

    @Override
    public Ticker getTickerInfo(String ticker) {
        if (!this.tickerDetailsRepository.validateTicker(ticker)) {
            log.info("Ticker doesn't exist, requesting information to Polygon");
            TickerDetails tickerDetails = this.stockDataRepository.getTickerOverview(ticker);
            log.info("Saving ticker");
            this.tickerDetailsRepository.addTicker(tickerDetails);
        }
        return this.tickerDetailsRepository.getTicker(ticker).get(NumberUtils.INTEGER_ZERO);
    }

    @Override
    public AssetDailyPricing getStockDailyPricing() {
        return this.stockDataRepository.getStockDailyPricing().block();
    }
}
