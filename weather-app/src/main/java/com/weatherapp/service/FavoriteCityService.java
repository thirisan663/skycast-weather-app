package com.weatherapp.service;

import java.util.List;

import com.weatherapp.dto.FavoriteCityDto;

public interface FavoriteCityService {
    List<FavoriteCityDto> getFavorites(String sessionToken);
    FavoriteCityDto addFavorite(String sessionToken, double lat, double lon, String cityName, String country, String state);
    void removeFavorite(String sessionToken, Long favoriteId);
}
