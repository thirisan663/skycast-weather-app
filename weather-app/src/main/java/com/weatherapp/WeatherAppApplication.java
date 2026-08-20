package com.weatherapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point for the AI-Powered Weather Dashboard.
 * <p>
 * Boots the embedded server, wires up JPA repositories, Thymeleaf view
 * resolution, and enables method-level caching used by {@code WeatherService}
 * to avoid re-fetching identical current-weather/forecast calls.
 */
@SpringBootApplication
@EnableCaching
public class WeatherAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAppApplication.class, args);
    }
}
