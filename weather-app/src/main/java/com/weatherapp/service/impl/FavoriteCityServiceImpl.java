package com.weatherapp.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weatherapp.dto.CityDto;
import com.weatherapp.dto.FavoriteCityDto;
import com.weatherapp.dto.WeatherResponseDto;
import com.weatherapp.entity.City;
import com.weatherapp.entity.FavoriteCity;
import com.weatherapp.entity.User;
import com.weatherapp.repository.CityRepository;
import com.weatherapp.repository.FavoriteCityRepository;
import com.weatherapp.service.FavoriteCityService;
import com.weatherapp.service.UserService;
import com.weatherapp.service.WeatherService;

import lombok.RequiredArgsConstructor;

/**
 * Manages a user's pinned cities and enriches each one with a live
 * mini weather snapshot for the Favorites page.
 */
@Service
@RequiredArgsConstructor
public class FavoriteCityServiceImpl implements FavoriteCityService {

    private final FavoriteCityRepository favoriteCityRepository;
    private final CityRepository cityRepository;
    private final UserService userService;
    private final WeatherService weatherService;

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteCityDto> getFavorites(String sessionToken) {
        User user = userService.getOrCreateUser(sessionToken);
        return favoriteCityRepository.findByUserOrderBySortOrderAsc(user).stream()
                .map(this::toDtoWithLiveSnapshot)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FavoriteCityDto addFavorite(String sessionToken, double lat, double lon, String cityName, String country, String state) {
        User user = userService.getOrCreateUser(sessionToken);

        City city = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(cityName, country)
                .orElseGet(() -> cityRepository.save(new City(null, cityName, state, country, lat, lon)));

        if (favoriteCityRepository.existsByUserAndCity(user, city)) {
            FavoriteCity existing = favoriteCityRepository.findByUserAndCity(user, city).orElseThrow();
            return toDtoWithLiveSnapshot(existing);
        }

        FavoriteCity favorite = new FavoriteCity();
        favorite.setUser(user);
        favorite.setCity(city);
        favorite.setSortOrder(favoriteCityRepository.findByUserOrderBySortOrderAsc(user).size());

        return toDtoWithLiveSnapshot(favoriteCityRepository.save(favorite));
    }

    @Override
    @Transactional
    public void removeFavorite(String sessionToken, Long favoriteId) {
        User user = userService.getOrCreateUser(sessionToken);
        favoriteCityRepository.findById(favoriteId)
                .filter(f -> f.getUser().getId().equals(user.getId()))
                .ifPresent(favoriteCityRepository::delete);
    }

    private FavoriteCityDto toDtoWithLiveSnapshot(FavoriteCity favorite) {
        City city = favorite.getCity();
        CityDto cityDto = CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .state(city.getState())
                .country(city.getCountry())
                .latitude(city.getLatitude())
                .longitude(city.getLongitude())
                .build();

        FavoriteCityDto.FavoriteCityDtoBuilder builder = FavoriteCityDto.builder()
                .favoriteId(favorite.getId())
                .city(cityDto);

        try {
            WeatherResponseDto snapshot = weatherService.getByCoordinates(
                    city.getLatitude(), city.getLongitude(), city.getName(), city.getCountry());
            builder.currentTemperature(snapshot.getCurrent().getTemperature())
                    .currentCondition(snapshot.getCurrent().getCondition())
                    .iconCode(snapshot.getCurrent().getIconCode());
        } catch (Exception ex) {
            // Live snapshot is a nice-to-have on this page; if the provider call fails,
            // still return the favorite itself rather than breaking the whole list.
            builder.currentCondition("Unavailable");
        }

        return builder.build();
    }
}
