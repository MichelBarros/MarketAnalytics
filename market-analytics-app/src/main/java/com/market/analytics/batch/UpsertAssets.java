package com.market.analytics.batch;

import com.market.analytics.api.StockDataRepository;
import com.market.analytics.db.AssetsRepository;
import com.market.analytics.domain.TickerSymbols;
import com.market.analytics.exception.TaskletExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Slf4j
public class UpsertAssets implements Tasklet, StepExecutionListener {

    private StockDataRepository stockDataRepository;
    private AssetsRepository assetsRepository;

    @Autowired
    private void setStockDataRepository(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    @Autowired
    private void setTickerDetailsRepository(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Rest list of ticker symbols");
        Optional<String> nextUrl = Optional.empty();

        do {
            TickerSymbols tickerSymbols = this.stockDataRepository.getAllTickers(nextUrl).block();
             this.assetsRepository.upsertAssets(tickerSymbols);
            nextUrl = Optional.ofNullable(tickerSymbols.nextUrl());
        } while (nextUrl.isPresent());
        log.info("Assets Upserting Completed");

        return RepeatStatus.FINISHED;
    }

}
