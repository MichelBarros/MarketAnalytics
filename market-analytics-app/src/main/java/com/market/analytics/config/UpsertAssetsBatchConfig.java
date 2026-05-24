package com.market.analytics.config;

import com.market.analytics.batch.UpsertAssets;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UpsertAssetsBatchConfig {

    @Bean
    public Job assetsIncrementalJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("assetsIncrementalJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(upsertAssetsStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step upsertAssetsStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("upsertAssetsTasklet", jobRepository)
                .tasklet(upsertAssets(), platformTransactionManager)
                .build();
    }

    @Bean
    public UpsertAssets upsertAssets() {
        return new UpsertAssets();
    }

}
