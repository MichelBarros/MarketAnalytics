package com.market.analytics.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "market-analytics")
public class MarketAnalyticsConfigProperties {

    private BlockVolume blockVolume;
    private BatchConfig batchConfig;
    private Apis apis;

    @Data
    public static class BlockVolume {
        private String dataSourcePath;
        private String dataStoragePath;
    }

    @Data
    public static class BatchConfig {
        private int mirroringChunkSize;
        private int dataRetentionLapse;
    }

    @Data
    public static class Apis {
        private BulkData bulkData;
    }

    @Data
    public static class BulkData {
        private QueryParams queryParams;
        private String baseUrl;
        private String stocksDaily;
        private String forexDaily;
        private String cryptoDaily;
        private String tickerOverview;
        private String allTickers;
        private int delay;
    }

    @Data
    public static class QueryParams {
        private Boolean adjusted;
        private Boolean includeOtc;
        private int limit;
        private String apiKey;
    }

}
