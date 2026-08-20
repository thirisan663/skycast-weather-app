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
public class UserPreferenceDto {
    private String temperatureUnit;
    private String windSpeedUnit;
    private String theme;
    private Long defaultCityId;
}
