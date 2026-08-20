package com.weatherapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Incoming chat message from the AI Weather Assistant widget. The
 * lat/lon + cityName let the service ground its answer in the weather
 * data currently shown on screen, rather than guessing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDto {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String cityName;
    private Double latitude;
    private Double longitude;
}
