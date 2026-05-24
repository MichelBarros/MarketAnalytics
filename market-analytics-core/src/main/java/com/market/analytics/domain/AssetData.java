package com.market.analytics.domain;

import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * @author Michel Barros
 */
@Builder(toBuilder = true)
public record AssetData(
        String ticker,
        String providerTicker,
        String per,
        OffsetDateTime dateTime,
        Double open,
        Double high,
        Double low,
        Double close,
        Double vol,
        Double volumeWeighted,
        boolean otc,
        Double rsi,
        Double diPos,
        Double diNeg,
        Double adx,
        Double atr,
        Double volatility) {

    public String toCsvWithIndicators() {
        String separator = ",";
        StringBuilder sb = new StringBuilder();

        sb.append(this.ticker).append(separator);
        sb.append(this.providerTicker).append(separator);
        sb.append(this.per).append(separator);
        sb.append(this.dateTime.toString()).append(separator);
        sb.append(this.open).append(separator);
        sb.append(this.high).append(separator);
        sb.append(this.low).append(separator);
        sb.append(this.close).append(separator);
        sb.append(this.vol).append(separator);
        sb.append(this.volumeWeighted).append(separator);
        sb.append(this.otc).append(separator);

        sb.append(formatDecimal(this.rsi)).append(separator);

        sb.append(formatDecimal(this.diPos)).append(separator);

        sb.append(formatDecimal(this.diNeg)).append(separator);

        sb.append(formatDecimal(this.adx)).append(separator);

        sb.append(formatDecimal(this.atr)).append(separator);

        sb.append(formatDecimal(this.volatility));

        return sb.toString();
    }

    private String formatDecimal(Double value) {
        return value != null ? String.format(Locale.US, "%.4f", value) : "";
    }

}
