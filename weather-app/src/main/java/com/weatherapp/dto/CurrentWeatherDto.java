package com.weatherapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Everything the "current conditions" section of the dashboard needs.
 * Populated by WeatherService from the external provider's response -
 * never built directly from a JPA entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherDto {
    private String cityName;
    private String country;
    private Double latitude;
    private Double longitude;

    private Double temperature;
    private Double feelsLike;
    private Double tempMin;
    private Double tempMax;

    private String condition;        // e.g. "Clear", "Rain"
    private String conditionDetail;  // e.g. "light rain"
    private String iconCode;         // provider icon code, mapped to our own icon set client-side

    private Integer humidity;        // %
    private Double windSpeed;        // km/h
    private Integer windDirectionDeg;
    private String windDirectionCompass; // "NE", "SW" etc.
    private Double pressure;         // hPa
    private Double visibility;       // km
    private Double uvIndex;

    private LocalDateTime sunrise;
    private LocalDateTime sunset;
    private LocalDateTime observedAt;
    private String timezone;
}
