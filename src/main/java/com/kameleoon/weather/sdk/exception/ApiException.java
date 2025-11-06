package com.kameleoon.weather.sdk.exception;

import lombok.Getter;

/**
 * Исключение для ошибок API OpenWeatherMap
 */
@Getter
public class ApiException extends WeatherSdkException {
    private final int statusCode;
    private final String apiMessage;

    public ApiException(int statusCode, String apiMessage, String userMessage) {
        super(userMessage);
        this.statusCode = statusCode;
        this.apiMessage = apiMessage;
    }

}