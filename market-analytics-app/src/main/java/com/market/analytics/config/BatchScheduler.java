package com.market.analytics.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    private final Job dailyPricingJob;

    private final Job assetsIncrementalJob;

    public BatchScheduler(JobLauncher jobLauncher, Job dailyPricingJob, Job assetsIncrementalJob) {
        this.jobLauncher = jobLauncher;
        this.dailyPricingJob = dailyPricingJob;
        this.assetsIncrementalJob = assetsIncrementalJob;
    }

//    @Scheduled(cron = "0 30 0 * * 1")
//    @Scheduled(cron = "0 46 17 * * *")
    public void scheduleAssetsIncrementalJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("executionTime", System.currentTimeMillis())
                .addString("jobName", "assetsIncrementalJob")
                .toJobParameters();

        this.jobLauncher.run(assetsIncrementalJob, jobParameters);
    }

//    @Scheduled(cron = "0 5 5 * * *")
    @Scheduled(cron = "0 36 2 * * *")
    public void scheduleDailyPricingJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("executionTime", System.currentTimeMillis())
                .addString("jobName", "dailyPricingJob")
                .toJobParameters();

        this.jobLauncher.run(dailyPricingJob, jobParameters);
    }

}
