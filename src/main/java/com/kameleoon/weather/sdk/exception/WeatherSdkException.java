package com.kameleoon.weather.sdk.exception;

/**
 * Базовое исключение SDK для всех ошибок, связанных с получением погоды
 */
public class WeatherSdkException extends Exception {
    public WeatherSdkException(String message) {
        super(message);
    }

    public WeatherSdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
