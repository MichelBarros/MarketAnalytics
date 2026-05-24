package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class V1_2__insertAssets extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        PGConnection pgConnection = connection.unwrap(PGConnection.class);
        CopyManager copyManager = pgConnection.getCopyAPI();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("data/assets.csv"),
                        StandardCharsets.UTF_8
                )
        )) {

            String copySql = """
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
                FROM STDIN WITH (FORMAT csv, HEADER true)
                """;

            copyManager.copyIn(copySql, reader);
        }

        try (Statement stmt = connection.createStatement()) {

            stmt.execute("""
                INSERT INTO assets (
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
                    ticker_type_id,
                    last_updated_utc
                )
                SELECT
                    s.ticker,
                    s.name,
                    s.market,
                    s.locale,
                    s.primary_exchange,
                    s.type,
                    s.active,
                    s.currency_symbol,
                    s.currency_name,
                    s.cik,
                    s.composite_figi,
                    s.share_class_figi,
                    tt.id AS ticker_type_id,
                    s.last_updated_utc
                FROM assets_staging s
                JOIN ticker_types tt
                  ON tt.code = s.type
                 AND tt.locale = s.locale
            """);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE assets_staging");
        }
    }
}
