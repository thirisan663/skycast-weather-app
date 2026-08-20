package com.weatherapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "weather.api")
public class WeatherApiProperties {
    private String baseUrl;
    private String geoUrl;
    private String key;
    private String units;
    private long timeoutMs;
}
