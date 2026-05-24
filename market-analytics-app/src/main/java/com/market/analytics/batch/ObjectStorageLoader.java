package com.market.analytics.batch;

import com.market.analytics.batch.utils.ObjectStorageLoaderUtils;
import com.market.analytics.config.MarketAnalyticsConfigProperties;
import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.AssetData;
import com.market.analytics.exception.TaskletExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ObjectStorageLoader implements Tasklet, StepExecutionListener {

    private List<String> files = new ArrayList<>();

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

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Stock Pricing Reading from Storage {}.", this.marketAnalyticsConfigProperties.getBlockVolume().getDataSourcePath());
        File sourceDirectory = new File(this.marketAnalyticsConfigProperties.getBlockVolume().getDataSourcePath());
        ObjectStorageLoaderUtils.mapFilesPath(sourceDirectory, files);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            log.info("Stock Pricing Writing to Structured Data {}.", this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath());
            ObjectStorageLoaderUtils.createDataStructuredFile(files, this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath());
            List<AssetData> assetDataList = ObjectStorageLoaderUtils.appendDataToStructuredFile(
                    files,
                    this.marketAnalyticsConfigProperties.getBlockVolume().getDataStoragePath(),
                    this.marketAnalyticsConfigProperties.getBatchConfig().getDataRetentionLapse()
            );
            this.dataHistoryRepository.saveAssetDataHistory(assetDataList);
            this.dataHistoryRepository.saveIndicatorsHistory(assetDataList);

            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            throw new TaskletExecutionException("Object storage loader failed");
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Deleting Source Files.");
        ObjectStorageLoaderUtils.deleteSourceFiles(files);
        return ExitStatus.COMPLETED;
    }

}
