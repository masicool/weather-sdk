package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.client.OpenWeatherMapClient;
import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.WeatherData;

/**
 * Сервис погоды в режиме on-demand (по требованию)
 * Выполняет непосредственные запросы к API OpenWeatherMap при каждом обращении.
 * Не использует кеширование - каждый запрос приводит к обращению к внешнему сервису.
 * Рекомендуется для сценариев с редкими запросами или когда необходимы всегда актуальные данные.
 */
public class OnDemandWeatherService implements WeatherService {
    private final OpenWeatherMapClient client;

    /**
     * Создает сервис on-demand режима с указанной конфигурацией
     *
     * @throws IllegalArgumentException если конфигурация некорректна
     */
    public OnDemandWeatherService(OpenWeatherMapClient client) {
        this.client = client;
    }

    /**
     * Получает данные о погоде для указанного города.
     * Выполняет непосредственный запрос к API OpenWeatherMap без использования кеша.
     * Гарантирует получение самых актуальных данных, но может быть медленнее
     * из-за сетевых задержек и ограничений API.
     *
     * @param cityName название города на любом языке
     * @return данные о погоде для указанного города
     * @throws WeatherSdkException      если произошла ошибка при обращении к API,
     *                                  город не найден или превышены лимиты запросов
     * @throws IllegalArgumentException если название города пустое или null
     */
    @Override
    public WeatherData getWeather(String cityName) throws WeatherSdkException {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }
        return client.getWeatherByCityName(cityName);
    }
}
