package com.market.analytics.config;

import com.market.analytics.batch.AdxIndicator;
import com.market.analytics.batch.AssetItemWriter;
import com.market.analytics.batch.CleanUpDataBase;
import com.market.analytics.batch.CryptoPricing;
import com.market.analytics.batch.ForexPricing;
import com.market.analytics.batch.RsiIndicator;
import com.market.analytics.batch.StockPricing;
import com.market.analytics.batch.ObjectStorageLoader;
import com.market.analytics.db.mappers.AssetDataRowMapper;
import com.market.analytics.domain.AssetData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class PricingBatchConfig {

    private MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;
    private AssetDataRowMapper mapper;

    @Autowired
    private void setMarketAnalyticsConfigProperties(MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    @Autowired
    private void setAssetDataRowMapper(AssetDataRowMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public Job dailyPricingJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step mirroringStep) {
        return new JobBuilder("dailyPricingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(objectStorageLoader(jobRepository, transactionManager))
                .next(stockPricing(jobRepository, transactionManager))
                .next(forexPricing(jobRepository, transactionManager))
                .next(cryptoPricing(jobRepository, transactionManager))
                .next(cleanUpDataHistory(jobRepository, transactionManager))
                .next(calculateRsiIndicator(jobRepository, transactionManager))
                .next(calculateAdxIndicator(jobRepository, transactionManager))
                .next(mirroringStep)
                .build();
    }

    @Bean
    public Step objectStorageLoader(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("objectStorageLoaderTasklet", jobRepository)
                .tasklet(loader(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public ObjectStorageLoader loader() {
        return new ObjectStorageLoader();
    }

    @Bean
    public Step stockPricing(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("stockPricingTasklet", jobRepository)
                .tasklet(stockReader(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public StockPricing stockReader() {
        return new StockPricing();
    }

    @Bean
    public Step forexPricing(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("forexPricingTasklet", jobRepository)
                .tasklet(forexReader(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public ForexPricing forexReader() {
        return new ForexPricing();
    }

    @Bean
    public Step cryptoPricing(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("cryptoPricingTasklet", jobRepository)
                .tasklet(cryptoReader(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public CryptoPricing cryptoReader() {
        return new CryptoPricing();
    }

    @Bean
    public Step cleanUpDataHistory(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("cleanUpDataHistoryTasklet", jobRepository)
                .tasklet(cleanUpData(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public CleanUpDataBase cleanUpData() {
        return new CleanUpDataBase();
    }

    @Bean
    public Step calculateRsiIndicator(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("rsiIndicatorTasklet", jobRepository)
                .tasklet(rsiIndicator(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public RsiIndicator rsiIndicator() {
        return new RsiIndicator();
    }

    @Bean
    public Step calculateAdxIndicator(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("adxIndicatorTasklet", jobRepository)
                .tasklet(adxIndicator(), platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public AdxIndicator adxIndicator() {
        return new AdxIndicator();
    }

    @Bean
    public Step mirroringStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              JdbcCursorItemReader<AssetData> reader,
                              AssetItemWriter writer) {
        return new StepBuilder("mirroringStep", jobRepository)
                .<AssetData, AssetData>chunk(
                        this.marketAnalyticsConfigProperties.getBatchConfig().getMirroringChunkSize(),
                        transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .retryLimit(3)
                .retry(java.io.IOException.class) // Resiliencia ante fallos de disco
                .build();
    }

    @Bean
    public JdbcCursorItemReader<AssetData> reader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AssetData>()
                .name("assetEnrichedReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT dh.*, ih.rsi, ih.dipos, ih.dineg, ih.adx, ih.atr, ih.volatility
                        FROM data_history dh
                        LEFT JOIN indicators_history ih
                            ON dh.asset_id = ih.asset_id
                            AND dh.period = ih.period
                            AND dh.time = ih.time
                        WHERE dh.time >= (NOW() AT TIME ZONE 'UTC')::date - INTERVAL '1 day'
                        ORDER BY dh.provider_ticker ASC, dh.time ASC;
                    """)
                .rowMapper(mapper)
                .fetchSize(this.marketAnalyticsConfigProperties.getBatchConfig().getMirroringChunkSize())
                .build();
    }

    @Bean
    public AssetItemWriter writer() {
        return new AssetItemWriter();
    }

}
