package com.weatherapp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate payload returned by /api/weather endpoints and rendered by the
 * dashboard page. Wraps current conditions + hourly + 7-day forecast so the
 * frontend only needs a single round trip per city search.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDto {
    private CurrentWeatherDto current;
    private List<HourlyForecastDto> hourly;
    private List<DailyForecastDto> daily;
}
