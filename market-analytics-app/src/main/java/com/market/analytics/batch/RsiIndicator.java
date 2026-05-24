package com.market.analytics.batch;

import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.Temporality;
import com.market.analytics.exception.TaskletExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

@Slf4j
public class RsiIndicator implements Tasklet {

    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private void setDataHistoryRepository(DataHistoryRepository dataHistoryRepository) {
        this.dataHistoryRepository = dataHistoryRepository;
    }

    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("Calculating RSI indicator.");
            this.dataHistoryRepository.rsiIndicator(Temporality.D);
        } catch (Exception e) {
            throw new TaskletExecutionException("RSI calculation failed");
        }
        return RepeatStatus.FINISHED;
    }

}
