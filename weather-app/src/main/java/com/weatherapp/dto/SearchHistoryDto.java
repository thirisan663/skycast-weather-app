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
public class SearchHistoryDto {
    private Long id;
    private CityDto city;
    private LocalDateTime searchedAt;
    private Double temperatureAtSearch;
    private String conditionAtSearch;
}
