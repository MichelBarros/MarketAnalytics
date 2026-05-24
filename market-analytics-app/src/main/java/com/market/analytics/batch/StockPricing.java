package com.market.analytics.batch;

import com.market.analytics.api.StockDataRepository;
import com.market.analytics.batch.utils.AssetCommonUtils;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.AssetClass;
import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.Temporality;
import com.market.analytics.exception.TaskletExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StockPricing implements Tasklet, StepExecutionListener {

    private StockDataRepository stockDataRepository;
    private DataHistoryRepository dataHistoryRepository;
    private MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    @Autowired
    private void setStockDataRepository(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    @Autowired
    private void setDataHistoryRepository(DataHistoryRepository dataHistoryRepository) {
        this.dataHistoryRepository = dataHistoryRepository;
    }

    @Autowired
    private void setMarketAnalyticsConfigProperties(MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    private AssetDailyPricing stockDailyPricing;
    private List<AssetData> dataHistory = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Rest Stock Daily Pricing Reading");
        stockDailyPricing = this.stockDataRepository.getStockDailyPricing().block();
        log.info("Stock Daily Price Reading Completed with {} Rows", stockDailyPricing.count());
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("Rest Stock Daily Pricing Writer");
            if (stockDailyPricing.resultsCount() != NumberUtils.INTEGER_ZERO) {
                log.info("Parsing Stock Daily Pricing Data");
                stockDailyPricing.results().forEach(result -> {
                            AssetCommonUtils.addDataHistory(
                                    dataHistory,
                                    result,
                                    AssetClass.STOCK,
                                    Temporality.D
                            );
                            AssetCommonUtils.createDataStructuredFile(
                                    result.ticker(),
                                    this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath(),
                                    AssetClass.STOCK
                            );
                        }
                );
                stockDailyPricing = null;
                this.dataHistoryRepository.saveAssetDataHistory(dataHistory);
                log.info("Stock Daily Price Writing Completed");
            }
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            throw new TaskletExecutionException("Stock pricing failed");
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Rows Writed in DDBB: {}", dataHistory.size());
        dataHistory.clear();
        log.info("Stock Daily Pricing Task Completed");
        return ExitStatus.COMPLETED;
    }

}
