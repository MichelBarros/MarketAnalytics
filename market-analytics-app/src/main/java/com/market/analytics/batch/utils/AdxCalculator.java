package com.market.analytics.batch.utils;

import com.market.analytics.domain.AdxResult;

public class AdxCalculator {

    private final int period = 14;
    private int count = 0;

    // Precios anteriores
    private double lastHigh, lastLow, lastClose;

    // Valores suavizados (Wilder)
    private double str, sdmP, sdmN, sdmDX;

    // Para la semilla (SMA inicial)
    private double sumTR, sumDMP, sumDMN, sumDX;

    public AdxResult nextValue(double high, double low, double close) {
        count++;

        // Primer registro: solo inicializamos precios previos
        if (count == 1) {
            lastHigh = high;
            lastLow = low;
            lastClose = close;
            return new AdxResult(null, null, null, null, null);
        }

        // 1. Calcular TR, +DM y -DM
        double tr = Math.max(high - low, Math.max(Math.abs(high - lastClose), Math.abs(low - lastClose)));
        double upMove = high - lastHigh;
        double downMove = lastLow - low;

        double dmP = (upMove > downMove && upMove > 0) ? upMove : 0;
        double dmN = (downMove > upMove && downMove > 0) ? downMove : 0;

        lastHigh = high;
        lastLow = low;
        lastClose = close;

        // 2. Fase de Suavizado para DI y ATR (Wilder / RMA)
        if (count <= period + 1) {
            sumTR += tr;
            sumDMP += dmP;
            sumDMN += dmN;
            if (count == period + 1) {
                str = sumTR / period; // Primer ATR (Semilla)
                sdmP = sumDMP / period;
                sdmN = sumDMN / period;
            } else {
                // No hay datos suficientes ni para ATR ni para DI
                return new AdxResult(null, null, null, null, null);
            }
        } else {
            // Suavizado continuo
            str = (str * (period - 1) + tr) / period; // Este es el ATR
            sdmP = (sdmP * (period - 1) + dmP) / period;
            sdmN = (sdmN * (period - 1) + dmN) / period;
        }

        // 3. Calcular +DI, -DI y ATR/Volatility
        double diP = 100 * (sdmP / str);
        double diN = 100 * (sdmN / str);

        // El ATR es el valor suavizado del True Range (str)
        double currentAtr = str;
        // Volatilidad porcentual relativa al precio de cierre
        double currentVolatility = (currentAtr / close) * 100;

        // 4. Calcular DX (necesario para el ADX final)
        double dx = 100 * (Math.abs(diP - diN) / (diP + diN == 0 ? 1 : diP + diN));

        // 5. Fase de Suavizado para ADX (Necesita otros 14 periodos de DX)
        Double adxFinal = null;
        if (count <= (period * 2)) {
            sumDX += dx;
            if (count == (period * 2)) {
                sdmDX = sumDX / period;
                adxFinal = sdmDX;
            }
        } else {
            sdmDX = (sdmDX * (period - 1) + dx) / period;
            adxFinal = sdmDX;
        }

        return new AdxResult(diP, diN, adxFinal, currentAtr, currentVolatility);
    }

}
