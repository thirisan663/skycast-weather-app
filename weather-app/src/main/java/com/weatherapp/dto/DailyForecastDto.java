package com.weatherapp.dto;

import java.time.LocalDate;

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
public class DailyForecastDto {
    private LocalDate date;
    private String dayLabel;         // "Mon", "Tue" ... computed once server-side
    private Double tempMin;
    private Double tempMax;
    private String condition;
    private String iconCode;
    private Integer precipitationChance;
    private Integer humidity;
    private Double windSpeed;
}
