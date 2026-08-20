package com.weatherapp.service;

import java.util.List;

import com.weatherapp.dto.SearchHistoryDto;

public interface SearchHistoryService {
    List<SearchHistoryDto> getHistory(String sessionToken);
    void clearHistory(String sessionToken);
}
