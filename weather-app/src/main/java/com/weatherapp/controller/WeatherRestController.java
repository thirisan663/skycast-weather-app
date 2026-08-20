package com.weatherapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weatherapp.dto.CityDto;
import com.weatherapp.dto.CitySearchRequestDto;
import com.weatherapp.dto.WeatherResponseDto;
import com.weatherapp.service.GeocodingService;
import com.weatherapp.service.WeatherService;
import com.weatherapp.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * JSON API consumed by static/js/weather.js. Kept separate from
 * PageController so the two concerns (page rendering vs data fetching)
 * don't get tangled.
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherRestController {

    private final WeatherService weatherService;
    private final GeocodingService geocodingService;

    /** Full dashboard payload (current + hourly + daily) for a free-text search. */
    @PostMapping("/search")
    public ResponseEntity<WeatherResponseDto> search(@Valid @RequestBody CitySearchRequestDto request,
                                                       HttpServletRequest httpRequest,
                                                       HttpServletResponse httpResponse) {
        String sessionToken = SessionUtil.resolveSessionToken(httpRequest, httpResponse);
        return ResponseEntity.ok(weatherService.searchByQuery(request.getQuery(), sessionToken));
    }

    /** Fetch by known coordinates, e.g. for a "use my location" button. */
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherResponseDto> byCoordinates(@RequestParam double lat,
                                                              @RequestParam double lon,
                                                              @RequestParam(required = false) String cityName) {
        return ResponseEntity.ok(weatherService.getByCoordinates(lat, lon, cityName, null));
    }

    /** Typeahead suggestions as the user types in the search box. */
    @GetMapping("/suggest")
    public ResponseEntity<List<CityDto>> suggest(@RequestParam String query) {
        return ResponseEntity.ok(geocodingService.search(query, 5));
    }
}
