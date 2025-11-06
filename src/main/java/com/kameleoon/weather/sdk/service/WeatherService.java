package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.WeatherData;

/**
 * Базовый интерфейс сервиса для получения данных о погоде.
 *
 * <p>Определяет контракт для различных реализаций сервиса погоды,
 * включая режимы on-demand и polling.</p>
 */
public interface WeatherService {

    /**
     * Получение данных о погоде для указанного города.
     *
     * @param cityName название города на любом языке
     * @return объект {@link WeatherData} с данными о погоде
     * @throws WeatherSdkException      если произошла ошибка SDK
     * @throws IllegalArgumentException если название города пустое или null
     */
    WeatherData getWeather(String cityName) throws WeatherSdkException;
}
