package com.market.analytics.batch.utils;

public class RsiCalculator {

    private double lastPrice;
    private double avgGain;
    private double avgLoss;
    private int periodCount = 0;
    private final int period = 14;

    public Double nextValue(double currentPrice) {
        periodCount++;
        if (periodCount == 1) {
            lastPrice = currentPrice;
            return null; // No hay cambio el primer día
        }

        double change = currentPrice - lastPrice;
        double gain = Math.max(0, change);
        double loss = Math.max(0, -change);

        if (periodCount <= period + 1) {
            // Fase de "Calentamiento": Media Simple (SMA)
            avgGain += gain / period;
            avgLoss += loss / period;
            lastPrice = currentPrice;
            return (periodCount == period + 1) ? calculateRsi() : null;
        } else {
            // Fase de "Suavizado de Wilder" (RMA)
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
            lastPrice = currentPrice;
            return calculateRsi();
        }
    }

    private double calculateRsi() {
        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

}
