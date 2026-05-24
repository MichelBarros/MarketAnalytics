package com.market.analytics;

import com.market.analytics.config.MarketAnalyticsConfigProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * @author Michel Barros
 */
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(MarketAnalyticsConfigProperties.class)
public class MarketAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketAnalyticsApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
