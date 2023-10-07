package com.mts.cloud.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Autowired
    public ApplicationProperties applicationProperties;

    @Bean
    public WebClient webClientInit() {
        return WebClient.builder()
                .baseUrl(applicationProperties.baseUrl())
                .build();
    }
}
