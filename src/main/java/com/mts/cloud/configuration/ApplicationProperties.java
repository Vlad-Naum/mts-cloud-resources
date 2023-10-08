package com.mts.cloud.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationProperties(prefix = "application")
@EnableScheduling
public record ApplicationProperties(String baseUrl, String token, int minVm, int minDb) {
}
