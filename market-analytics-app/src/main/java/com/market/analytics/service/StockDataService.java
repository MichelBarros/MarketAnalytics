package com.market.analytics.service;

import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.Ticker;

public interface StockDataService {

    Ticker getTickerInfo(String ticker);

    AssetDailyPricing getStockDailyPricing();

}
