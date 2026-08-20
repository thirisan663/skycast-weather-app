package com.weatherapp.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weatherapp.dto.CityDto;
import com.weatherapp.dto.SearchHistoryDto;
import com.weatherapp.entity.City;
import com.weatherapp.entity.User;
import com.weatherapp.repository.SearchHistoryRepository;
import com.weatherapp.service.SearchHistoryService;
import com.weatherapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<SearchHistoryDto> getHistory(String sessionToken) {
        User user = userService.getOrCreateUser(sessionToken);
        return searchHistoryRepository.findByUserOrderBySearchedAtDesc(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearHistory(String sessionToken) {
        User user = userService.getOrCreateUser(sessionToken);
        searchHistoryRepository.deleteByUser(user);
    }

    private SearchHistoryDto toDto(com.weatherapp.entity.SearchHistory history) {
        City city = history.getCity();
        return SearchHistoryDto.builder()
                .id(history.getId())
                .city(CityDto.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .state(city.getState())
                        .country(city.getCountry())
                        .latitude(city.getLatitude())
                        .longitude(city.getLongitude())
                        .build())
                .searchedAt(history.getSearchedAt())
                .temperatureAtSearch(history.getTemperatureAtSearch())
                .conditionAtSearch(history.getConditionAtSearch())
                .build();
    }
}
