package com.weatherapp.exception;

/** Thrown when the AI provider call fails. Caught and translated into a graceful chat error bubble. */
public class AiAssistantException extends RuntimeException {
    public AiAssistantException(String message, Throwable cause) {
        super(message, cause);
    }
}
