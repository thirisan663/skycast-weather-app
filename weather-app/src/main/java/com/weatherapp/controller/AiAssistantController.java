package com.weatherapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weatherapp.dto.AiChatRequestDto;
import com.weatherapp.dto.AiChatResponseDto;
import com.weatherapp.service.AiAssistantService;
import com.weatherapp.util.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/ask")
    public ResponseEntity<AiChatResponseDto> ask(@Valid @RequestBody AiChatRequestDto request,
                                                   HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse) {
        String sessionToken = SessionUtil.resolveSessionToken(httpRequest, httpResponse);
        return ResponseEntity.ok(aiAssistantService.ask(request, sessionToken));
    }
}
