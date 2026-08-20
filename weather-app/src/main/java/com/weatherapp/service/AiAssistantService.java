package com.weatherapp.service;

import com.weatherapp.dto.AiChatRequestDto;
import com.weatherapp.dto.AiChatResponseDto;

/**
 * Answers natural-language weather questions grounded in the currently
 * displayed conditions (umbrella / clothing / outdoor-activity advice etc.).
 */
public interface AiAssistantService {
    AiChatResponseDto ask(AiChatRequestDto request, String sessionToken);
}
