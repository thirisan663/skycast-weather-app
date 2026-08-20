package com.weatherapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weatherapp.dto.FavoriteCityDto;
import com.weatherapp.service.FavoriteCityService;
import com.weatherapp.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteRestController {

    private final FavoriteCityService favoriteCityService;

    @GetMapping
    public ResponseEntity<List<FavoriteCityDto>> list(HttpServletRequest req, HttpServletResponse res) {
        String sessionToken = SessionUtil.resolveSessionToken(req, res);
        return ResponseEntity.ok(favoriteCityService.getFavorites(sessionToken));
    }

    @PostMapping
    public ResponseEntity<FavoriteCityDto> add(@RequestBody Map<String, Object> body,
                                                HttpServletRequest req, HttpServletResponse res) {
        String sessionToken = SessionUtil.resolveSessionToken(req, res);
        double lat = ((Number) body.get("latitude")).doubleValue();
        double lon = ((Number) body.get("longitude")).doubleValue();
        String name = (String) body.get("cityName");
        String country = (String) body.get("country");
        String state = (String) body.get("state");
        return ResponseEntity.ok(favoriteCityService.addFavorite(sessionToken, lat, lon, name, country, state));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> remove(@PathVariable Long favoriteId,
                                        HttpServletRequest req, HttpServletResponse res) {
        String sessionToken = SessionUtil.resolveSessionToken(req, res);
        favoriteCityService.removeFavorite(sessionToken, favoriteId);
        return ResponseEntity.noContent().build();
    }
}
