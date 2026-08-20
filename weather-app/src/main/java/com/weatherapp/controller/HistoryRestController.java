package com.weatherapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weatherapp.dto.SearchHistoryDto;
import com.weatherapp.service.SearchHistoryService;
import com.weatherapp.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryRestController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public ResponseEntity<List<SearchHistoryDto>> list(HttpServletRequest req, HttpServletResponse res) {
        String sessionToken = SessionUtil.resolveSessionToken(req, res);
        return ResponseEntity.ok(searchHistoryService.getHistory(sessionToken));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(HttpServletRequest req, HttpServletResponse res) {
        String sessionToken = SessionUtil.resolveSessionToken(req, res);
        searchHistoryService.clearHistory(sessionToken);
        return ResponseEntity.noContent().build();
    }
}
