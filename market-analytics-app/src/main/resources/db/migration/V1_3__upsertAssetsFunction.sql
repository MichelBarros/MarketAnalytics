CREATE OR REPLACE FUNCTION upsert_assets(
    p_ticker VARCHAR,
    p_name VARCHAR,
    p_market VARCHAR,
    p_locale VARCHAR,
    p_primary_exchange VARCHAR,
    p_type VARCHAR,
    p_active BOOLEAN,
    p_currency_symbol VARCHAR,
    p_currency_name VARCHAR,
    p_cik INTEGER,
    p_composite_figi VARCHAR,
    p_share_class_figi VARCHAR,
    p_base_currency_symbol VARCHAR,
    p_base_currency_name VARCHAR,
    p_last_updated_utc TIMESTAMPTZ
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    v_ticker_type_id SMALLINT;
BEGIN
    -- Obtener ticker_type_id desde tabla de control
    SELECT id
    INTO v_ticker_type_id
    FROM ticker_types
    WHERE code = p_type
    LIMIT 1;

    IF v_ticker_type_id IS NULL THEN
        RAISE EXCEPTION 'Ticker type not found for code: %', p_type;
    END IF;

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
    VALUES (
        p_ticker,
        p_name,
        p_market,
        p_locale,
        p_primary_exchange,
        p_type,
        v_ticker_type_id,
        p_active,
        p_currency_symbol,
        p_currency_name,
        p_cik,
        p_composite_figi,
        p_share_class_figi,
        p_base_currency_symbol,
        p_base_currency_name,
        p_last_updated_utc
    )
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

END;
$$;
