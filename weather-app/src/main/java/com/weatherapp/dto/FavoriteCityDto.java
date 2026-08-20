package com.weatherapp.dto;

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
public class FavoriteCityDto {
    private Long favoriteId;
    private CityDto city;
    /** Optional live snapshot so the favorites page can show a mini weather chip. */
    private Double currentTemperature;
    private String currentCondition;
    private String iconCode;
}
