package com.market.analytics.batch;

import com.market.analytics.api.ForexDataRepository;
import com.market.analytics.batch.utils.AssetCommonUtils;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.AssetClass;
import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.AssetData;
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
public class ForexPricing implements Tasklet, StepExecutionListener {

    private ForexDataRepository forexDataRepository;
    private DataHistoryRepository dataHistoryRepository;
    private MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    @Autowired
    private void setForexDataRepository(ForexDataRepository forexDataRepository) {
        this.forexDataRepository = forexDataRepository;
    }

    @Autowired
    private void setDataHistoryRepository(DataHistoryRepository dataHistoryRepository) {
        this.dataHistoryRepository = dataHistoryRepository;
    }

    @Autowired
    private void setMarketAnalyticsConfigProperties(MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    private AssetDailyPricing forexDailyPricing;
    private List<AssetData> dataHistory = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Rest Forex Daily Pricing Reading.");
        forexDailyPricing = this.forexDataRepository.getForexDailyPricing().block();
        log.info("Forex Daily Price Reading Completed with {} Rows.", forexDailyPricing.count());
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("Rest Forex Daily Pricing Writer.");
            if (forexDailyPricing.resultsCount() != NumberUtils.INTEGER_ZERO) {
                log.info("Parsing Forex Daily Pricing Data");
                forexDailyPricing.results().forEach(result -> {
                            AssetCommonUtils.addDataHistory(
                                    dataHistory,
                                    result,
                                    AssetClass.FOREX,
                                    Temporality.D
                            );
                            AssetCommonUtils.createDataStructuredFile(
                                    result.ticker(),
                                    this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath(),
                                    AssetClass.FOREX
                            );
                        }
                );
                forexDailyPricing = null;
                this.dataHistoryRepository.saveAssetDataHistory(dataHistory);
                log.info("Forex Daily Price Writing Completed");
            }
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            throw new TaskletExecutionException("Forex pricing failed");
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Rows Writed in DDBB: {}", dataHistory.size());
        dataHistory.clear();
        log.info("Forex Daily Pricing Task Completed");
        return ExitStatus.COMPLETED;
    }

}