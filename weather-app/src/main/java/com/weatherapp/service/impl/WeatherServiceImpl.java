package com.weatherapp.service.impl;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weatherapp.dto.CityDto;
import com.weatherapp.dto.CurrentWeatherDto;
import com.weatherapp.dto.WeatherResponseDto;
import com.weatherapp.entity.City;
import com.weatherapp.entity.SearchHistory;
import com.weatherapp.entity.User;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.repository.CityRepository;
import com.weatherapp.repository.SearchHistoryRepository;
import com.weatherapp.service.GeocodingService;
import com.weatherapp.service.UserService;
import com.weatherapp.service.WeatherService;

import lombok.RequiredArgsConstructor;

/**
 * Orchestrates: geocode -> fetch weather -> persist search history.
 * This is the single entry point the controllers call for weather data.
 */
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final GeocodingService geocodingService;
    private final WeatherProviderClient providerClient;
    private final CityRepository cityRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserService userService;

    @Override
    @Transactional
    public WeatherResponseDto searchByQuery(String query, String sessionToken) {
        List<CityDto> matches = geocodingService.search(query, 1);
        if (matches.isEmpty()) {
            throw new CityNotFoundException(query);
        }
        CityDto match = matches.get(0);
        WeatherResponseDto response = getByCoordinates(match.getLatitude(), match.getLongitude(), match.getName(), match.getCountry());

        recordSearch(sessionToken, match, response.getCurrent());
        return response;
    }

    @Override
    @Cacheable(value = "currentWeather", key = "#lat + ',' + #lon")
    public WeatherResponseDto getByCoordinates(double lat, double lon, String cityName, String country) {
        CurrentWeatherDto current = providerClient.fetchCurrent(lat, lon, cityName, country);

        List<com.weatherapp.dto.HourlyForecastDto> hourly;
        try {
            hourly = providerClient.fetchHourly(lat, lon);
        } catch (Exception ex) {
            hourly = List.of();
        }

        List<com.weatherapp.dto.DailyForecastDto> daily;
        try {
            daily = providerClient.fetchDaily(lat, lon);
        } catch (Exception ex) {
            daily = List.of(); // /onecall needs a separate paid subscription on free-tier keys
        }

        return WeatherResponseDto.builder()
                .current(current)
                .hourly(hourly)
                .daily(daily)
                .build();
    }

    private void recordSearch(String sessionToken, CityDto matchedCity, CurrentWeatherDto current) {
        User user = userService.getOrCreateUser(sessionToken);

        City city = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(matchedCity.getName(), matchedCity.getCountry())
                .orElseGet(() -> cityRepository.save(new City(null, matchedCity.getName(), matchedCity.getState(),
                        matchedCity.getCountry(), matchedCity.getLatitude(), matchedCity.getLongitude())));

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setCity(city);
        history.setTemperatureAtSearch(current.getTemperature());
        history.setConditionAtSearch(current.getCondition());
        searchHistoryRepository.save(history);
    }
}
