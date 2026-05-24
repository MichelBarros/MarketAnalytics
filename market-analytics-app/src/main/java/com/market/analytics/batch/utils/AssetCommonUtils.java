package com.market.analytics.batch.utils;

import com.market.analytics.domain.AssetCandleInformation;
import com.market.analytics.domain.AssetClass;
import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.Temporality;
import com.market.analytics.exception.DataStructuredFileNotCreated;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
public final class AssetCommonUtils {

    private static final String DATA_STRUCTURED_EXTENSION = ".csv";

    public static void addDataHistory(
            List<AssetData> dataHistory,
            AssetCandleInformation assetCandleInformation,
            AssetClass assetClass,
            Temporality temporality) {
        dataHistory.add(
                AssetData.builder()
                        .ticker(normalizeTicker(assetCandleInformation.ticker(), assetClass))
                        .providerTicker(assetCandleInformation.ticker())
                        .per(temporality.name())
                        .dateTime(parseDate(assetCandleInformation.time()))
                        .open(assetCandleInformation.open())
                        .high(assetCandleInformation.high())
                        .low(assetCandleInformation.low())
                        .close(assetCandleInformation.close())
                        .vol(assetCandleInformation.volume())
                        .volumeWeighted(assetCandleInformation.volumeWeightedAverage())
                        .otc(assetCandleInformation.otc())
                        .build()
        );
    }

    public static void createDataStructuredFile(
            String ticker,
            String dataStoragePath,
            AssetClass assetClass) {
        String filePath = dataStoragePath + File.separator +
                assetClass.market + File.separator +
                normalizeTicker(ticker, assetClass) + DATA_STRUCTURED_EXTENSION;
        File newFile = new File(filePath);
        try {
            if (newFile.createNewFile()) {
                log.debug("File Created: {}", filePath);
                writeCsvHeader(newFile);
            }
        } catch (IOException e) {
            throw new DataStructuredFileNotCreated();
        }
    }

    public static void writeCsvHeader(File file) throws IOException {
        String header = "ticker,providerTicker,period,dateTime,open,high,low,close,volume,volumeWeighted,otc,rsi,diPos,diNeg,adx,atr,volatility";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(header);
            writer.newLine();
        }
    }

    private static OffsetDateTime parseDate(Long time) {
        return Instant.ofEpochMilli(time)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime();
    }

    private static String normalizeTicker(
            String tickerName,
            AssetClass assetClass) {
        if (assetClass.assetPrefix.isEmpty()) {
            return tickerName;
        }
        return tickerName.replace(assetClass.assetPrefix, "");
    }

}
