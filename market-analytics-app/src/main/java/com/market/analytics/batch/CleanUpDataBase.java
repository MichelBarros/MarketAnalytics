package com.market.analytics.batch;

import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.Temporality;
import com.market.analytics.exception.TaskletExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

public class CleanUpDataBase implements Tasklet {

    private DataHistoryRepository dataHistoryRepository;
    private MarketAnalyticsConfigProperties marketAnalyticsConfigProperties;

    @Autowired
    private void setDataHistoryRepository(DataHistoryRepository dataHistoryRepository) {
        this.dataHistoryRepository = dataHistoryRepository;
    }

    @Autowired
    private void setMarketAnalyticsConfigProperties(MarketAnalyticsConfigProperties marketAnalyticsConfigProperties) {
        this.marketAnalyticsConfigProperties = marketAnalyticsConfigProperties;
    }

    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            this.dataHistoryRepository.cleanUpDataHistory(
                    Temporality.D,
                    this.marketAnalyticsConfigProperties.getBatchConfig().getDataRetentionLapse()
            );
        } catch (Exception e) {
            throw new TaskletExecutionException("Clean up data history failed");
        }
        return RepeatStatus.FINISHED;
    }

}
