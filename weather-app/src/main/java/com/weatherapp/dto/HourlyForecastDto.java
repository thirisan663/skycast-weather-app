package com.weatherapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyForecastDto {
    private LocalDateTime time;
    private Double temperature;
    private String condition;
    private String iconCode;
    private Integer precipitationChance; // %
    private Double windSpeed;
}
