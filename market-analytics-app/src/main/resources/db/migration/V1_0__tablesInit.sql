CREATE TABLE IF NOT EXISTS ticker_types (
  id SMALLINT GENERATED ALWAYS AS IDENTITY,
  asset_class VARCHAR(25) NOT NULL,
  code VARCHAR(25) NOT NULL,
  description VARCHAR(50) NOT NULL,
  locale VARCHAR(10) NOT NULL,
  CONSTRAINT pk_ticker_types PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ticker_types_idx ON ticker_types (asset_class, code, locale);


CREATE TABLE IF NOT EXISTS assets (
  id INTEGER GENERATED ALWAYS AS IDENTITY,
  ticker VARCHAR(20) NOT NULL,
  name VARCHAR(250) NOT NULL,
  market VARCHAR(25) NOT NULL,
  locale VARCHAR(10) NOT NULL,
  primary_exchange VARCHAR(15) NULL,
  type VARCHAR(10) NOT NULL,
  ticker_type_id SMALLINT NOT NULL,
  active BOOLEAN NOT NULL,
  currency_symbol VARCHAR(10) NULL,
  currency_name VARCHAR(50) NULL,
  cik INTEGER NULL,
  composite_figi VARCHAR(45) NULL,
  share_class_figi VARCHAR(45) NULL,
  base_currency_symbol VARCHAR(25) NULL,
  base_currency_name VARCHAR(50) NULL,
  last_updated_utc TIMESTAMPTZ NULL,
  market_cap BIGINT NULL,
  description VARCHAR(1000) NULL,
  sic_code VARCHAR(10) NULL,
  sic_description VARCHAR(45) NULL,
  ticker_root VARCHAR(20) NULL,
  homepage_url VARCHAR(45) NULL,
  total_employees INTEGER NULL,
  list_date DATE NULL,
  logo_url VARCHAR(200) NULL,
  icon_url VARCHAR(200) NULL,
  share_class_shares_outstanding BIGINT NULL,
  weighted_shares_outstanding BIGINT NULL,
  round_lot INTEGER NULL,
  CONSTRAINT pk_assets PRIMARY KEY (id),
  CONSTRAINT fk_assets_ticker_type FOREIGN KEY (ticker_type_id) REFERENCES ticker_types(id)
);
CREATE UNIQUE INDEX assets_idx ON assets (ticker, name, market, locale, type, ticker_type_id, market_cap);
CREATE UNIQUE INDEX ux_assets_ticker_market ON assets (ticker, market);


CREATE UNLOGGED TABLE IF NOT EXISTS assets_staging (
    ticker VARCHAR(20),
    name VARCHAR(250),
    market VARCHAR(25),
    locale VARCHAR(10),
    primary_exchange VARCHAR(15),
    type VARCHAR(10),
    active BOOLEAN,
    currency_symbol VARCHAR(10),
    currency_name VARCHAR(50),
    cik INTEGER,
    composite_figi VARCHAR(45),
    share_class_figi VARCHAR(45),
    base_currency_symbol VARCHAR(25),
    base_currency_name VARCHAR(50),
    last_updated_utc TIMESTAMPTZ NULL
);


CREATE TABLE IF NOT EXISTS data_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    ticker VARCHAR(20) NOT NULL,
    provider_ticker VARCHAR(20) NOT NULL,
    asset_id INTEGER NOT NULL,
    period VARCHAR(10) NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    close NUMERIC(18,8) NOT NULL,
    high NUMERIC(18,8) NOT NULL,
    low NUMERIC(18,8) NOT NULL,
    open NUMERIC(18,8) NOT NULL,
    otc BOOLEAN NULL,
    volume NUMERIC(20,4) NOT NULL,
    volume_weighted NUMERIC(20,4) NOT NULL,
    CONSTRAINT pk_data_history PRIMARY KEY (id),
    CONSTRAINT fk_data_history_asset FOREIGN KEY (asset_id) REFERENCES assets(id)
);
CREATE UNIQUE INDEX data_history_unique_idx ON data_history (asset_id, period, time);


CREATE UNLOGGED TABLE IF NOT EXISTS data_history_staging (
    ticker VARCHAR(20) NOT NULL,
    provider_ticker VARCHAR(20) NOT NULL,
    period VARCHAR(10) NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    close NUMERIC(18,8) NOT NULL,
    high NUMERIC(18,8) NOT NULL,
    low NUMERIC(18,8) NOT NULL,
    open NUMERIC(18,8) NOT NULL,
    otc BOOLEAN NULL,
    volume NUMERIC(20,4) NOT NULL,
    volume_weighted NUMERIC(20,4) NOT NULL
);
CREATE INDEX idx_staging_ticker_otc ON data_history_staging (ticker, otc);


CREATE TABLE IF NOT EXISTS indicators_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    ticker VARCHAR(20) NOT NULL,
    provider_ticker VARCHAR(20) NOT NULL,
    asset_id INTEGER NOT NULL,
    period VARCHAR(10) NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    rsi NUMERIC(10,4) NULL,
    dipos NUMERIC(10,4) NULL,
    dineg NUMERIC(10,4) NULL,
    adx NUMERIC(10,4) NULL,
    atr NUMERIC(10,4) NULL,
    volatility NUMERIC(10,4) NULL,
    CONSTRAINT pk_indicators_history PRIMARY KEY (id),
    CONSTRAINT fk_indicators_history_asset FOREIGN KEY (asset_id) REFERENCES assets(id)
);
CREATE UNIQUE INDEX indicators_history_idx ON indicators_history (asset_id, period, time DESC);

CREATE UNLOGGED TABLE IF NOT EXISTS indicators_history_staging (
    ticker VARCHAR(20),
    provider_ticker VARCHAR(20),
    period VARCHAR(10),
    time TIMESTAMPTZ,
    rsi NUMERIC(10,4),
    dipos NUMERIC(10,4),
    dineg NUMERIC(10,4),
    adx NUMERIC(10,4),
    atr NUMERIC(10,4),
    volatility NUMERIC(10,4),
    otc BOOLEAN
);
CREATE INDEX idx_indicators_staging_ticker_otc ON indicators_history_staging (ticker, otc);