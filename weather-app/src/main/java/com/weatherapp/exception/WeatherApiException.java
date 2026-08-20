package com.weatherapp.exception;

/** Thrown when the external weather provider call fails or returns an unexpected shape. */
public class WeatherApiException extends RuntimeException {
    public WeatherApiException(String message) {
        super(message);
    }
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
