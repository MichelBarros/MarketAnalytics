package com.market.analytics.db;

import com.market.analytics.db.mappers.AssetDataRowMapper;
import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.AssetDataParams;
import com.market.analytics.domain.Temporality;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author Michel Barros
 */
@Slf4j
@Repository
public class DataHistoryRepositoryImpl implements DataHistoryRepository {

    private static final String ASSET_DATA_QUERY = "SELECT * FROM data_history WHERE ticker = ? and per = ? ORDER BY date DESC LIMIT ?,?;";
    // SELECT * FROM dataHistory where ticker = 'AAPL' and per = 'D' order by date, time limit 0, 15
    private static final String SAVE_HISTORY_DATA_QUERY = "INSERT INTO data_history_staging " +
            "(ticker, provider_ticker, period, time, close, high, low, open, otc, volume, volume_weighted) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SAVE_INDICATORS_HISTORY_DATA_QUERY = "INSERT INTO indicators_history_staging " +
            "(ticker, provider_ticker, period, time, rsi, dipos, dineg, adx, atr, volatility, otc) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//    INSERT INTO data_history
//            (asset_id, period, time, close, high, low, open, otc, volume, volume_weighted)
//    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//    ON CONFLICT (asset_id, period, time)
//    DO NOTHING;

    private final JdbcTemplate jdbcTemplate;
    private final AssetDataRowMapper mapper;

    public DataHistoryRepositoryImpl(JdbcTemplate jdbcTemplate, AssetDataRowMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public List<AssetData> getAssetData(AssetDataParams pagination) {
        log.info("Getting data history paginated");
        return this.jdbcTemplate.query(
                ASSET_DATA_QUERY,
                mapper::mapRow,
                pagination.ticker(),
                pagination.temporality().getPer(),
                calculatePageNumber(pagination),
                pagination.pageSize()
        );
    }

    @Override
    @Transactional
    public void saveAssetDataHistory(List<AssetData> dataHistory) {
        log.info("Saving data history");
        this.jdbcTemplate.batchUpdate(
                SAVE_HISTORY_DATA_QUERY,
                dataHistory,
                1000,
                (PreparedStatement ps, AssetData row) -> {
                    ps.setString(1, row.ticker());
                    ps.setString(2, row.providerTicker());
                    ps.setString(3, row.per());
                    ps.setObject(4, row.dateTime());
                    ps.setDouble(5, row.close());
                    ps.setDouble(6, row.high());
                    ps.setDouble(7, row.low());
                    ps.setDouble(8, row.open());
                    ps.setBoolean(9, row.otc());
                    ps.setDouble(10, row.vol());
                    ps.setDouble(11, row.volumeWeighted());
                }
        );
        this.jdbcTemplate.update("""
                INSERT INTO data_history (
                    ticker,
                    provider_ticker,
                    asset_id,
                    period,
                    time,
                    close,
                    high,
                    low,
                    open,
                    otc,
                    volume,
                    volume_weighted
                )
                select
                    s.ticker,
                    s.provider_ticker,
                    a.id,
                    s.period,
                    s.time,
                    s.close,
                    s.high,
                    s.low,
                    s.open,
                    s.otc,
                    s.volume,
                    s.volume_weighted
                FROM data_history_staging s
                JOIN assets a
                    ON a.ticker = s.provider_ticker
                   AND (
                        (s.otc = TRUE  AND a.market = 'otc')
                        OR
                        (s.otc IS DISTINCT FROM TRUE AND a.market <> 'otc')
                   )
                ON CONFLICT (asset_id, period, time)
                DO NOTHING;
                """);

        this.jdbcTemplate.update("TRUNCATE data_history_staging");
    }

    @Override
    public void cleanUpDataHistory(Temporality temporality, int timeLapse) {
        log.info("Cleaning up data history");
        String query = """
        DELETE FROM data_history
        WHERE id IN (
            SELECT id
            FROM (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY provider_ticker
                           ORDER BY "time" DESC
                       ) as fila_num
                FROM data_history
                WHERE period = ?
            ) as subconsulta_numerada
            WHERE fila_num > ?
        )
        """;
        int deletedRows = this.jdbcTemplate.update(query, temporality.name(), timeLapse);
        log.info("Old rows deleted {} for period {} {}", deletedRows, timeLapse, temporality.name());
    }

    @Override
    public void rsiIndicator(Temporality temporality) {
        log.info("Calculating 14 periods RSI indicator in temporality {}", temporality.name());
        String query = """
                WITH RECURSIVE\s
                -- 1. Numeramos las filas para poder iterar sobre ellas
                indexed_data AS (
                    SELECT\s
                        asset_id, ticker, provider_ticker, period, time, close,
                        COALESCE(close - LAG(close) OVER (PARTITION BY asset_id, period ORDER BY time ASC), 0) as diff,
                        ROW_NUMBER() OVER (PARTITION BY asset_id, period ORDER BY time ASC) as rn
                    FROM data_history
                    WHERE period = ? -- Aquí pasas tu Temporality (ej. 'D')
                ),
                -- 2. Identificamos ganancias y pérdidas
                gains_losses AS (
                    SELECT *,
                           CASE WHEN diff > 0 THEN diff ELSE 0 END as gain,
                           CASE WHEN diff < 0 THEN ABS(diff) ELSE 0 END as loss
                    FROM indexed_data
                ),
                -- 3. Punto de partida: El primer RSI se calcula con media simple (SMA)
                seed AS (
                    SELECT\s
                        asset_id, ticker, provider_ticker, period, time, rn,
                        AVG(gain) OVER (PARTITION BY asset_id, period ORDER BY time ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_gain,
                        AVG(loss) OVER (PARTITION BY asset_id, period ORDER BY time ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_loss
                    FROM gains_losses
                    WHERE rn = 14 -- El primer valor válido es en la fila 14
                ),
                -- 4. RECURSIVIDAD: Aplicamos el suavizado de Wilder fila por fila
                wilder_recursive AS (
                    SELECT asset_id, ticker, provider_ticker, period, time, rn, avg_gain, avg_loss
                    FROM seed
                    UNION ALL
                    SELECT\s
                        gl.asset_id, gl.ticker, gl.provider_ticker, gl.period, gl.time, gl.rn,
                        (wr.avg_gain * 13 + gl.gain) / 14 as avg_gain,
                        (wr.avg_loss * 13 + gl.loss) / 14 as avg_loss
                    FROM gains_losses gl
                    JOIN wilder_recursive wr ON gl.asset_id = wr.asset_id AND gl.period = wr.period AND gl.rn = wr.rn + 1
                )
                -- 5. Inserción final con el cálculo del RSI
                INSERT INTO indicators_history (ticker, provider_ticker, asset_id, period, time, rsi)
                SELECT\s
                    ticker, provider_ticker, asset_id, period, time,
                    CASE\s
                        WHEN avg_loss = 0 THEN 100\s
                        ELSE 100 - (100 / (1 + (avg_gain / avg_loss)))\s
                    END as rsi
                FROM wilder_recursive
                ON CONFLICT (asset_id, period, time)\s
                DO UPDATE SET\s
                rsi = EXCLUDED.rsi
                WHERE indicators_history.rsi IS NULL;
        """;
        this.jdbcTemplate.update(query, temporality.name());
    }

    @Override
    public void adxIndicator(Temporality temporality) {
        log.info("Calculating 14 periods ADX indicator in temporality {}", temporality.name());
        String query = """
               INSERT INTO indicators_history (ticker, provider_ticker, asset_id, period, time, dipos, dineg, adx, atr, volatility)
               WITH RECURSIVE\s
                   base_data AS (
                       SELECT\s
                           asset_id, ticker, provider_ticker, period, time, high, low, close,
                           LAG(high) OVER (PARTITION BY asset_id, period ORDER BY time ASC) as prev_high,
                           LAG(low) OVER (PARTITION BY asset_id, period ORDER BY time ASC) as prev_low,
                           LAG(close) OVER (PARTITION BY asset_id, period ORDER BY time ASC) as prev_close,
                           ROW_NUMBER() OVER (PARTITION BY asset_id, period ORDER BY time ASC) as rn
                       FROM data_history
                       WHERE period = ?
                   ),
                   components AS (
                       SELECT *,
                           GREATEST(high - low, ABS(high - prev_close), ABS(low - prev_close)) as tr,
                           CASE WHEN (high - prev_high) > (prev_low - low) AND (high - prev_high) > 0\s
                                THEN (high - prev_high) ELSE 0 END as dm_pos,
                           CASE WHEN (prev_low - low) > (high - prev_high) AND (prev_low - low) > 0\s
                                THEN (prev_low - low) ELSE 0 END as dm_neg
                       FROM base_data
                       WHERE prev_high IS NOT NULL
                   ),
                   initial_stats AS (
                       SELECT *,
                              AVG(tr) OVER (PARTITION BY asset_id ORDER BY rn ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_tr,
                              AVG(dm_pos) OVER (PARTITION BY asset_id ORDER BY rn ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_dm_p,
                              AVG(dm_neg) OVER (PARTITION BY asset_id ORDER BY rn ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_dm_n
                       FROM components
                   ),
                   smoothed_dm AS (
                       SELECT asset_id, ticker, provider_ticker, period, time, rn, close, avg_tr as str, avg_dm_p as sdm_p, avg_dm_n as sdm_n
                       FROM initial_stats
                       WHERE rn = 15
                       UNION ALL
                       SELECT c.asset_id, c.ticker, c.provider_ticker, c.period, c.time, c.rn, c.close,
                              (s.str * 13 + c.tr) / 14,
                              (s.sdm_p * 13 + c.dm_pos) / 14,
                              (s.sdm_n * 13 + c.dm_neg) / 14
                       FROM components c
                       JOIN smoothed_dm s ON c.asset_id = s.asset_id AND c.period = s.period AND c.rn = s.rn + 1
                   ),
                   directional_indices AS (
                       SELECT *,
                              100 * (sdm_p / NULLIF(str, 0)) as di_pos,
                              100 * (sdm_n / NULLIF(str, 0)) as di_neg
                       FROM smoothed_dm
                   ),
                   dx_calculation AS (
                       SELECT *,
                              COALESCE(100 * ABS(COALESCE(di_pos,0) - COALESCE(di_neg,0)) / NULLIF(COALESCE(di_pos,0) + COALESCE(di_neg,0), 0), 0) as dx
                       FROM directional_indices
                   ),
                   adx_seed AS (
                       SELECT *,
                              AVG(dx) OVER (PARTITION BY asset_id ORDER BY rn ASC ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) as avg_adx
                       FROM dx_calculation
                   ),
                   adx_final AS (
                       SELECT asset_id, ticker, provider_ticker, period, time, rn, close, str as atr_value, di_pos, di_neg, avg_adx as adx
                       FROM adx_seed
                       WHERE rn = 28
                       UNION ALL
                       SELECT d.asset_id, d.ticker, d.provider_ticker, d.period, d.time, d.rn, d.close, d.str as atr_value, d.di_pos, d.di_neg,
                              (a.adx * 13 + d.dx) / 14
                       FROM dx_calculation d
                       JOIN adx_final a ON d.asset_id = a.asset_id AND d.period = a.period AND d.rn = a.rn + 1
                   )
               SELECT\s
                    ticker, provider_ticker, asset_id, period, time,\s
                    di_pos, di_neg, adx, atr_value as atr,
                    (atr_value / NULLIF(close, 0)) * 100 as volatility
               FROM adx_final
               ON CONFLICT (asset_id, period, time)\s
               DO UPDATE SET\s
                   dipos = EXCLUDED.dipos,
                   dineg = EXCLUDED.dineg,
                   adx = EXCLUDED.adx,
                   atr = EXCLUDED.atr,
                   volatility = EXCLUDED.volatility
               WHERE indicators_history.adx IS NULL OR indicators_history.adx = 0; -- Agregamos OR = 0 por seguridad
            """;
        this.jdbcTemplate.update(query, temporality.name());
    }

    @Override
    @Transactional
    public void saveIndicatorsHistory(List<AssetData> indicatorData) {
        log.info("Saving indicators history");

        this.jdbcTemplate.batchUpdate(
                SAVE_INDICATORS_HISTORY_DATA_QUERY,
                indicatorData,
                1000,
                (PreparedStatement ps, AssetData row) -> {
                    ps.setString(1, row.ticker());
                    ps.setString(2, row.providerTicker());
                    ps.setString(3, row.per());
                    ps.setObject(4, row.dateTime());
                    ps.setObject(5, row.rsi());
                    ps.setObject(6, row.diPos());
                    ps.setObject(7, row.diNeg());
                    ps.setObject(8, row.adx());
                    ps.setObject(9, row.atr());
                    ps.setObject(10, row.volatility());
                    ps.setBoolean(11, row.otc());
                }
        );

        this.jdbcTemplate.update("""
            INSERT INTO indicators_history (
                ticker, provider_ticker, asset_id, period, time, 
                rsi, dipos, dineg, adx, atr, volatility
            )
            SELECT 
                s.ticker, s.provider_ticker, a.id, s.period, s.time,
                s.rsi, s.dipos, s.dineg, s.adx, s.atr, s.volatility
            FROM indicators_history_staging s
            JOIN assets a 
                ON a.ticker = s.provider_ticker
               AND (
                    (s.otc = TRUE AND a.market = 'otc')
                    OR 
                    (s.otc IS DISTINCT FROM TRUE AND a.market <> 'otc')
               )
            ON CONFLICT (asset_id, period, time) 
            DO UPDATE SET 
                rsi = EXCLUDED.rsi,
                dipos = EXCLUDED.dipos,
                dineg = EXCLUDED.dineg,
                adx = EXCLUDED.adx,
                atr = EXCLUDED.atr,
                volatility = EXCLUDED.volatility;
            """);

        this.jdbcTemplate.update("TRUNCATE indicators_history_staging");
    }

    private int calculatePageNumber(AssetDataParams pagination) {
        return (pagination.pageNumber() - NumberUtils.INTEGER_ONE) * pagination.pageSize() + NumberUtils.INTEGER_ONE;
    }

}
