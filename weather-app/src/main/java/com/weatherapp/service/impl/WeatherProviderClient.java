package com.weatherapp.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.weatherapp.config.WeatherApiProperties;
import com.weatherapp.dto.CurrentWeatherDto;
import com.weatherapp.dto.DailyForecastDto;
import com.weatherapp.dto.HourlyForecastDto;
import com.weatherapp.exception.WeatherApiException;

import lombok.RequiredArgsConstructor;

/**
 * Low-level adapter around the external weather provider's REST API
 * (OpenWeatherMap-shaped: /weather, /forecast/hourly, /onecall/daily).
 * This is the ONLY class in the codebase that knows the provider's raw
 * JSON field names - everything else works with our DTOs. Swapping
 * providers means rewriting this one class.
 */
@Component
@RequiredArgsConstructor
class WeatherProviderClient {

    private final RestTemplate weatherRestTemplate;
    private final WeatherApiProperties properties;

    @SuppressWarnings("unchecked")
    CurrentWeatherDto fetchCurrent(double lat, double lon, String cityName, String country) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", properties.getUnits())
                .queryParam("appid", properties.getKey())
                .toUriString();
        try {
            Map<String, Object> raw = weatherRestTemplate.getForObject(url, Map.class);
            if (raw == null) {
                throw new WeatherApiException("Empty response from weather provider.");
            }
            return mapCurrent(raw, cityName, country, lat, lon);
        } catch (RestClientException ex) {
            throw new WeatherApiException("Unable to fetch current weather.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    List<HourlyForecastDto> fetchHourly(double lat, double lon) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/forecast")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", properties.getUnits())
                .queryParam("cnt", 8) // next 24h in 3h steps
                .queryParam("appid", properties.getKey())
                .toUriString();
        try {
            Map<String, Object> raw = weatherRestTemplate.getForObject(url, Map.class);
            if (raw == null) {
				return List.of();
			}
            List<Map<String, Object>> list = (List<Map<String, Object>>) raw.get("list");
            if (list == null) {
				return List.of();
			}
            return list.stream().map(this::mapHourly).collect(Collectors.toList());
        } catch (RestClientException ex) {
            throw new WeatherApiException("Unable to fetch hourly forecast.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    List<DailyForecastDto> fetchDaily(double lat, double lon) {
        // Providers vary here (One Call 3.0 requires a paid tier); this method
        // isolates that call so it can be swapped for a different endpoint/plan
        // without touching WeatherServiceImpl.
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/onecall")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", properties.getUnits())
                .queryParam("exclude", "minutely,alerts")
                .queryParam("appid", properties.getKey())
                .toUriString();
        try {
            Map<String, Object> raw = weatherRestTemplate.getForObject(url, Map.class);
            if (raw == null) {
				return List.of();
			}
            List<Map<String, Object>> list = (List<Map<String, Object>>) raw.get("daily");
            if (list == null) {
				return List.of();
			}
            return list.stream().limit(7).map(this::mapDaily).collect(Collectors.toList());
        } catch (RestClientException ex) {
            throw new WeatherApiException("Unable to fetch 7-day forecast.", ex);
        }
    }

    // ---------------------------------------------------------------
    // Mapping helpers: raw provider JSON -> our DTOs
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private CurrentWeatherDto mapCurrent(Map<String, Object> raw, String cityName, String country, double lat, double lon) {
        Map<String, Object> main = (Map<String, Object>) raw.get("main");
        Map<String, Object> wind = (Map<String, Object>) raw.get("wind");
        Map<String, Object> sys = (Map<String, Object>) raw.get("sys");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) raw.get("weather");
        Map<String, Object> weather = (weatherList != null && !weatherList.isEmpty()) ? weatherList.get(0) : Map.of();

        int windDeg = wind != null && wind.get("deg") != null ? ((Number) wind.get("deg")).intValue() : 0;

        return CurrentWeatherDto.builder()
                .cityName(cityName != null ? cityName : (String) raw.get("name"))
                .country(country != null ? country : (sys != null ? (String) sys.get("country") : null))
                .latitude(lat)
                .longitude(lon)
                .temperature(numberOrNull(main, "temp"))
                .feelsLike(numberOrNull(main, "feels_like"))
                .tempMin(numberOrNull(main, "temp_min"))
                .tempMax(numberOrNull(main, "temp_max"))
                .condition((String) weather.get("main"))
                .conditionDetail((String) weather.get("description"))
                .iconCode((String) weather.get("icon"))
                .humidity(main != null && main.get("humidity") != null ? ((Number) main.get("humidity")).intValue() : null)
                .windSpeed(numberOrNull(wind, "speed"))
                .windDirectionDeg(windDeg)
                .windDirectionCompass(toCompass(windDeg))
                .pressure(numberOrNull(main, "pressure"))
                .visibility(raw.get("visibility") != null ? ((Number) raw.get("visibility")).doubleValue() / 1000.0 : null)
                .uvIndex(null) // populated separately if the plan supports it; kept null-safe here
                .sunrise(sys != null && sys.get("sunrise") != null ? toLocalDateTime(sys.get("sunrise")) : null)
                .sunset(sys != null && sys.get("sunset") != null ? toLocalDateTime(sys.get("sunset")) : null)
                .observedAt(LocalDateTime.now())
                .timezone(raw.get("timezone") != null ? raw.get("timezone").toString() : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private HourlyForecastDto mapHourly(Map<String, Object> raw) {
        Map<String, Object> main = (Map<String, Object>) raw.get("main");
        Map<String, Object> wind = (Map<String, Object>) raw.get("wind");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) raw.get("weather");
        Map<String, Object> weather = (weatherList != null && !weatherList.isEmpty()) ? weatherList.get(0) : Map.of();
        Object pop = raw.get("pop"); // probability of precipitation, 0..1

        return HourlyForecastDto.builder()
                .time(toLocalDateTime(raw.get("dt")))
                .temperature(numberOrNull(main, "temp"))
                .condition((String) weather.get("main"))
                .iconCode((String) weather.get("icon"))
                .precipitationChance(pop != null ? (int) Math.round(((Number) pop).doubleValue() * 100) : 0)
                .windSpeed(numberOrNull(wind, "speed"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private DailyForecastDto mapDaily(Map<String, Object> raw) {
        Map<String, Object> temp = (Map<String, Object>) raw.get("temp");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) raw.get("weather");
        Map<String, Object> weather = (weatherList != null && !weatherList.isEmpty()) ? weatherList.get(0) : Map.of();
        Object pop = raw.get("pop");
        LocalDateTime date = toLocalDateTime(raw.get("dt"));

        return DailyForecastDto.builder()
                .date(date.toLocalDate())
                .dayLabel(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .tempMin(numberOrNull(temp, "min"))
                .tempMax(numberOrNull(temp, "max"))
                .condition((String) weather.get("main"))
                .iconCode((String) weather.get("icon"))
                .precipitationChance(pop != null ? (int) Math.round(((Number) pop).doubleValue() * 100) : 0)
                .humidity(raw.get("humidity") != null ? ((Number) raw.get("humidity")).intValue() : null)
                .windSpeed(numberOrNull(raw, "wind_speed"))
                .build();
    }

    private Double numberOrNull(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
			return null;
		}
        return ((Number) map.get(key)).doubleValue();
    }

    private LocalDateTime toLocalDateTime(Object epochSeconds) {
        long epoch = ((Number) epochSeconds).longValue();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }

    private String toCompass(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(((degrees % 360) / 22.5));
        return directions[index % 16];
    }
}
