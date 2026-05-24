package com.market.analytics.db;

import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.AssetDataParams;
import com.market.analytics.domain.Temporality;

import java.util.List;

/**
 * @author Michel Barros
 */
public interface DataHistoryRepository {

    List<AssetData> getAssetData(AssetDataParams pagination);

    void saveAssetDataHistory(List<AssetData> dataHistory);

    void cleanUpDataHistory(Temporality temporality, int timeLapse);

    void rsiIndicator(Temporality temporality);

    void adxIndicator(Temporality temporality);

    void saveIndicatorsHistory(List<AssetData> indicatorData);

}
