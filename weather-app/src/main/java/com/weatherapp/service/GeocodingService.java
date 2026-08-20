package com.weatherapp.service;

import java.util.List;

import com.weatherapp.dto.CityDto;

/** Resolves free-text location queries to coordinates via the provider's geocoding endpoint. */
public interface GeocodingService {
    List<CityDto> search(String query, int limit);
}
