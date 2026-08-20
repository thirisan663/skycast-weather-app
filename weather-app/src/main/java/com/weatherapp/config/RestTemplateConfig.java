package com.weatherapp.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final WeatherApiProperties weatherApiProperties;
    private final AiProviderProperties aiProviderProperties;

    @Bean
    public RestTemplate weatherRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(weatherApiProperties.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(weatherApiProperties.getTimeoutMs()))
                .build();
    }

    @Bean
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofMillis(aiProviderProperties.getTimeoutMs()))
                .build();
    }
}