package com.weatherapp.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp.config.AiProviderProperties;
import com.weatherapp.dto.AiChatRequestDto;
import com.weatherapp.dto.AiChatResponseDto;
import com.weatherapp.dto.CurrentWeatherDto;
import com.weatherapp.dto.WeatherResponseDto;
import com.weatherapp.exception.AiAssistantException;
import com.weatherapp.service.AiAssistantService;
import com.weatherapp.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds a grounding prompt from the *currently displayed* weather data
 * (so the model isn't guessing at conditions) and forwards the user's
 * question to the configured AI provider. If no live weather context is
 * available yet, falls back to a general-purpose weather-assistant prompt.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final RestTemplate aiRestTemplate;
    private final AiProviderProperties properties;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    @Override
    public AiChatResponseDto ask(AiChatRequestDto request, String sessionToken) {
        try {
            String systemPrompt = buildSystemPrompt(request);
            String reply = callAiProvider(systemPrompt, request.getMessage());
            return AiChatResponseDto.builder().reply(reply).success(true).build();
        } catch (AiAssistantException ex) {
        	log.warn("AI assistant call failed: {}", ex.getMessage());   // before
            log.warn("AI assistant call failed: {}", ex.getCause() != null ? ex.getCause().toString() : ex.getMessage()); // after
            throw ex;
        }
    }

    private String buildSystemPrompt(AiChatRequestDto request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a friendly, concise AI weather assistant embedded in a weather dashboard app. ")
                .append("Answer the user's question directly in 2-4 short sentences. ")
                .append("Give practical, actionable advice (umbrella, clothing, outdoor plans) when relevant. ")
                .append("Do not repeat raw numbers the user can already see on the dashboard unless it helps your answer. ")
                .append("Never invent weather data - only use what is provided below.\n\n");

        if (request.getLatitude() != null && request.getLongitude() != null) {
            try {
                WeatherResponseDto weather = weatherService.getByCoordinates(
                        request.getLatitude(), request.getLongitude(), request.getCityName(), null);
                CurrentWeatherDto c = weather.getCurrent();
                prompt.append("Current conditions for ").append(c.getCityName()).append(":\n")
                        .append("- Temperature: ").append(c.getTemperature()).append("C (feels like ").append(c.getFeelsLike()).append("C)\n")
                        .append("- Condition: ").append(c.getConditionDetail()).append("\n")
                        .append("- Humidity: ").append(c.getHumidity()).append("%\n")
                        .append("- Wind: ").append(c.getWindSpeed()).append(" km/h ").append(c.getWindDirectionCompass()).append("\n")
                        .append("- UV index: ").append(c.getUvIndex()).append("\n")
                        .append("- High/Low today: ").append(c.getTempMax()).append("C / ").append(c.getTempMin()).append("C\n");
                if (c.getSunrise() != null) {
                    prompt.append("- Sunrise: ").append(c.getSunrise().format(TIME_FMT)).append("\n");
                }
                if (c.getSunset() != null) {
                    prompt.append("- Sunset: ").append(c.getSunset().format(TIME_FMT)).append("\n");
                }
                if (weather.getHourly() != null && !weather.getHourly().isEmpty()) {
                    prompt.append("- Rain chance in next few hours: ")
                            .append(weather.getHourly().get(0).getPrecipitationChance()).append("%\n");
                }
            } catch (Exception ex) {
                prompt.append("(Live weather data for this location is currently unavailable - ")
                        .append("answer generally and suggest the user check the dashboard once data loads.)\n");
            }
        } else {
            prompt.append("(No specific city is currently selected - answer generally, ")
                    .append("or ask the user to search for a city first if the question needs specific data.)\n");
        }

        return prompt.toString();
    }

    private String callAiProvider(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        try {
            ResponseEntity<String> response = aiRestTemplate.exchange(
                    properties.getBaseUrl(), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("message").path("content");
            if (!content.isMissingNode()) {
                return content.asText();
            }
            throw new AiAssistantException("Unexpected AI provider response shape.", null);
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new AiAssistantException("AI provider call failed.", ex);
        }
    }
}
