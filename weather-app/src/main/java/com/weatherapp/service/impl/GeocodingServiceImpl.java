package com.weatherapp.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.weatherapp.config.WeatherApiProperties;
import com.weatherapp.dto.CityDto;
import com.weatherapp.exception.WeatherApiException;
import com.weatherapp.service.GeocodingService;

import lombok.RequiredArgsConstructor;

/**
 * Talks to the provider's geocoding endpoint (OpenWeatherMap Geo API shape:
 * GET /geo/1.0/direct?q={query}&limit={n}&appid={key}) and maps the raw
 * response into our own CityDto so nothing upstream depends on the
 * provider's JSON field names.
 */
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final RestTemplate weatherRestTemplate;
    private final WeatherApiProperties properties;

    @Override
    @SuppressWarnings("unchecked")
    public List<CityDto> search(String query, int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getGeoUrl() + "/direct")
                .queryParam("q", query)
                .queryParam("limit", limit)
                .queryParam("appid", properties.getKey())
                .toUriString();

        try {
            List<Map<String, Object>> results = weatherRestTemplate.getForObject(url, List.class);
            if (results == null) {
                return List.of();
            }
            return results.stream()
                    .map(this::toCityDto)
                    .collect(Collectors.toList());
        } catch (RestClientException ex) {
            throw new WeatherApiException("Unable to reach the geocoding service.", ex);
        }
    }

    private CityDto toCityDto(Map<String, Object> raw) {
        return CityDto.builder()
                .name((String) raw.get("name"))
                .state((String) raw.get("state"))
                .country((String) raw.get("country"))
                .latitude(((Number) raw.get("lat")).doubleValue())
                .longitude(((Number) raw.get("lon")).doubleValue())
                .build();
    }
}
