package com.market.analytics.batch;

import com.market.analytics.batch.utils.ObjectStorageLoaderUtils;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.domain.AssetClass;
import com.market.analytics.domain.AssetData;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class AssetItemWriter implements ItemWriter<AssetData> {

    private MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    @Autowired
    private void setMarketAnalyticsConfigProperties(MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    @Override
    public void write(@NonNull Chunk<? extends AssetData> chunk) throws Exception {
        for (AssetData item : chunk) {
            AssetClass assetClass = resolveMarket(item.providerTicker());
            String fileName = item.ticker() + ObjectStorageLoaderUtils.DATA_STRUCTURED_EXTENSION;

            FileWriter fileWriter = new FileWriter(
                    this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath() +
                            File.separator + assetClass.market + File.separator + fileName,
                    true
            );
            try (BufferedWriter bw = new BufferedWriter(fileWriter)) {
                bw.write(item.toCsvWithIndicators());
                bw.newLine();
            }
        }
    }

    private AssetClass resolveMarket(String providerTicker) {
        return AssetClass.getAssetFromProviderTicker(providerTicker);
    }

}
