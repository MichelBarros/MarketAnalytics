package com.market.analytics.db;

import com.market.analytics.domain.Ticker;
import com.market.analytics.domain.TickerDetails;
import com.market.analytics.domain.TickerSymbols;

import java.util.List;

public interface AssetsRepository {

    List<String> getAssetsColumns();

    void upsertAssets(TickerSymbols tickerSymbols);

    void addTicker(TickerDetails tickerDetails);

    boolean validateTicker(String ticker);

    List<Ticker> getTicker(String ticker);

}
