package com.market.analytics.domain;

import lombok.Getter;

@Getter
public enum AssetClass {

    STOCK(".us", ".US", "", "", "stock"),
    CRYPTO(".v", ".V", "USD", "X:", "crypto"),
    INDEX("^", "^", "", "I:", "index"),
    FOREX("", "", "", "C:", "forex");

    public final String fileNameSuffix;
    public final String tickerSuffix;
    public final String currency;
    public final String assetPrefix;
    public final String market;

    AssetClass(String fileNameSuffix, String tickerSuffix, String currency, String assetPrefix, String market) {
        this.fileNameSuffix = fileNameSuffix;
        this.tickerSuffix = tickerSuffix;
        this.currency = currency;
        this.assetPrefix = assetPrefix;
        this.market = market;
    }

    public static AssetClass getPrefixFromSuffix(String fileName) {
        for(AssetClass a : values()) {
            if (a != FOREX && fileName.contains(a.fileNameSuffix)) {
                return a;
            }
        }
        return FOREX;
    }

    public static AssetClass getAssetFromProviderTicker(String providerTicker) {
        for(AssetClass a : values()) {
            if (a != STOCK && providerTicker.contains(a.assetPrefix)) {
                return a;
            }
        }
        return STOCK;
    }

}
