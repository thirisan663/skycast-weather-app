package com.weatherapp.service;

import com.weatherapp.dto.WeatherResponseDto;

/**
 * Fetches current conditions + forecast for a place, and records the lookup
 * against the current user's search history.
 */
public interface WeatherService {

    /**
     * Resolves the free-text query (city, "city,country", zip, etc.) to
     * coordinates, fetches current + hourly + daily data, and logs the
     * search in MySQL for the given session.
     */
    WeatherResponseDto searchByQuery(String query, String sessionToken);

    /** Fetches weather for known coordinates (used by Favorites mini-cards and re-fetch flows). */
    WeatherResponseDto getByCoordinates(double lat, double lon, String cityName, String country);
}
