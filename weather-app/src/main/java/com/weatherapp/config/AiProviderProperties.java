package com.weatherapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.provider")
public class AiProviderProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private int maxTokens;
    private long timeoutMs;
}
