package com.kameleoon.weather.sdk.exception;

/**
 * Исключение когда город не найден (404 ошибка)
 */
public class CityNotFoundException extends ApiException {
    public CityNotFoundException(String cityName, String apiMessage) {
        super(404, apiMessage, "Город не найден: " + cityName);
    }
}