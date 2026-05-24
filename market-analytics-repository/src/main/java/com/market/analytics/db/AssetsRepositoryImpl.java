package com.market.analytics.db;

import com.market.analytics.db.mappers.TickerDetailsRowMapper;
import com.market.analytics.domain.ResultsTickerDetails;
import com.market.analytics.domain.Ticker;
import com.market.analytics.domain.TickerDetails;
import com.market.analytics.domain.TickerSymbols;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Michel Barros
 */
@Slf4j
@Repository
public class AssetsRepositoryImpl implements AssetsRepository {

    private static final String SAVE_TICKER_DETAILS_QUERY = "INSERT INTO assets " +
            "(ticker, name, market, locale, primary_exchange, type, active, currency_name, " +
            "cik, composite_figi, share_class_figi, market_cap, description, sic_code, sic_description, " +
            "ticker_root, homepage_url, total_employees, list_date, logo_url, icon_url, share_class_shares_outstanding, " +
            "weighted_shares_outstanding, round_lot) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String VALIDATE_TICKER_QUERY = "SELECT id FROM assets WHERE ticker = ?";
    private static final String GET_TICKER_QUERY = "SELECT * FROM assets WHERE ticker = ?";
    private static final String UPSERT_ASSETS_QUERY = "SELECT upsert_assets(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String GET_ASSETS_COLUMNS = "SELECT column_name \n" +
            "FROM information_schema.columns \n" +
            "WHERE table_schema = 'public' \n" +
            "AND table_name = 'assets_staging' ORDER BY ordinal_position";

    private static final char DELIMITER = '|';

    private final JdbcTemplate jdbcTemplate;
    private final TickerDetailsRowMapper mapper;

    public AssetsRepositoryImpl(JdbcTemplate jdbcTemplate, TickerDetailsRowMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }


    @Override
    public List<String> getAssetsColumns() {
        return this.jdbcTemplate.queryForList(
                GET_ASSETS_COLUMNS,
                String.class
        );
    }

    @Override
    public void upsertAssets(TickerSymbols tickerSymbols) {
        log.debug("Upserting assets");
        copyToStaging(tickerSymbols);
        upsertFromStaging();
    }

    @Override
    public void addTicker(TickerDetails tickerDetails) {
        log.info("Saving ticker details");
        this.jdbcTemplate.update(SAVE_TICKER_DETAILS_QUERY,
                tickerDetails.results().ticker(), tickerDetails.results().name(),
                tickerDetails.results().market(), tickerDetails.results().locale(),
                tickerDetails.results().primaryExchange(), tickerDetails.results().type(),
                tickerDetails.results().active(), tickerDetails.results().currencyName(),
                tickerDetails.results().cik(), tickerDetails.results().compositeFigi(),
                tickerDetails.results().shareClassFigi(), tickerDetails.results().marketCap(),
                tickerDetails.results().description(), tickerDetails.results().sicCode(),
                tickerDetails.results().sicDescription(), tickerDetails.results().tickerRoot(),
                tickerDetails.results().homepageUrl(), tickerDetails.results().totalEmployees(),
                tickerDetails.results().listDate(), tickerDetails.results().branding().logoUrl(),
                tickerDetails.results().branding().iconUrl(), tickerDetails.results().shareClassSharesOutstanding(),
                tickerDetails.results().weightedSharesOutstanding(), tickerDetails.results().roundLot());
    }

    @Override
    public boolean validateTicker(String ticker) {
        log.info("Validating ticker");
        List<Ticker> response = this.jdbcTemplate.query(
                VALIDATE_TICKER_QUERY,
                mapper::mapRow,
                ticker
        );
        return !response.isEmpty() && response.size() == NumberUtils.INTEGER_ONE;
    }

    @Override
    public List<Ticker> getTicker(String ticker) {
        log.info("Getting ticker information");
        return this.jdbcTemplate.query(
                GET_TICKER_QUERY,
                mapper::mapRow,
                ticker
        );
    }

    private String getType(ResultsTickerDetails row) {
        if (row.market().equals("fx")) {
            return "FX";
        } else if (row.market().equals("crypto")) {
            return "CRYPTO";
        }
        return row.type();
    }

    private void copyToStaging(TickerSymbols tickerSymbols) {
        log.debug("COPY assets_staging");

        this.jdbcTemplate.execute((Connection conn) -> {
            PGConnection pgConnection = conn.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();

            try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            buildCsvReader(tickerSymbols),
                            StandardCharsets.UTF_8
                    )
            )) {
                copyManager.copyIn(
                        """
                        COPY assets_staging (
                            ticker,
                            name,
                            market,
                            locale,
                            primary_exchange,
                            type,
                            active,
                            currency_symbol,
                            currency_name,
                            cik,
                            composite_figi,
                            share_class_figi,
                            base_currency_symbol,
                            base_currency_name,
                            last_updated_utc
                        )
                        FROM STDIN WITH (FORMAT csv, HEADER false, DELIMITER '|')
                        """,
                        reader
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private InputStream buildCsvReader(TickerSymbols tickerSymbols) {
        StringBuilder sb = new StringBuilder();

        for (ResultsTickerDetails row : tickerSymbols.results()) {
            appendNullable(sb, row.ticker()); sb.append(DELIMITER);
            appendNullable(sb, escape(row.name())); sb.append(DELIMITER);
            appendNullable(sb, row.market()); sb.append(DELIMITER);
            appendNullable(sb, row.locale()); sb.append(DELIMITER);
            appendNullable(sb, row.primaryExchange()); sb.append(DELIMITER);
            appendNullable(sb, getType(row)); sb.append(DELIMITER);
            appendNullable(sb, row.active()); sb.append(DELIMITER);
            appendNullable(sb, row.currencySymbol()); sb.append(DELIMITER);
            appendNullable(sb, row.currencyName()); sb.append(DELIMITER);
            appendNullable(sb, row.cik()); sb.append(DELIMITER);
            appendNullable(sb, row.compositeFigi()); sb.append(DELIMITER);
            appendNullable(sb, row.shareClassFigi()); sb.append(DELIMITER);
            appendNullable(sb, row.baseCurrencySymbol()); sb.append(DELIMITER);
            appendNullable(sb, row.baseCurrencyName()); sb.append(DELIMITER);
            appendNullable(sb, row.lastUpdatedUtc() != null ? Timestamp.from(row.lastUpdatedUtc()) : null);
            sb.append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return new ByteArrayInputStream(bytes);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private void appendNullable(StringBuilder sb, Object value) {
        if (value != null) {
            sb.append(value);
        }
    }

    private void upsertFromStaging() {
        log.debug("UPSERT assets from staging");

        jdbcTemplate.update("""
                INSERT INTO assets (
                    ticker,
                    name,
                    market,
                    locale,
                    primary_exchange,
                    type,
                    ticker_type_id,
                    active,
                    currency_symbol,
                    currency_name,
                    cik,
                    composite_figi,
                    share_class_figi,
                    base_currency_symbol,
                    base_currency_name,
                    last_updated_utc
                )
                SELECT
                    s.ticker,
                    s.name,
                    s.market,
                    s.locale,
                    s.primary_exchange,
                    s.type,
                    tt.id AS ticker_type_id,
                    s.active,
                    s.currency_symbol,
                    s.currency_name,
                    s.cik,
                    s.composite_figi,
                    s.share_class_figi,
                    s.base_currency_symbol,
                    s.base_currency_name,
                    s.last_updated_utc
                FROM assets_staging s
                JOIN ticker_types tt
                    ON tt.code = s.type
                ON CONFLICT (ticker, market)
                DO UPDATE SET
                    name = EXCLUDED.name,
                    locale = EXCLUDED.locale,
                    primary_exchange = EXCLUDED.primary_exchange,
                    type = EXCLUDED.type,
                    ticker_type_id = EXCLUDED.ticker_type_id,
                    active = EXCLUDED.active,
                    currency_symbol = EXCLUDED.currency_symbol,
                    currency_name = EXCLUDED.currency_name,
                    cik = EXCLUDED.cik,
                    composite_figi = EXCLUDED.composite_figi,
                    share_class_figi = EXCLUDED.share_class_figi,
                    base_currency_symbol = EXCLUDED.base_currency_symbol,
                    base_currency_name = EXCLUDED.base_currency_name,
                    last_updated_utc = EXCLUDED.last_updated_utc;
                """);

        jdbcTemplate.update("TRUNCATE assets_staging");
    }

}
