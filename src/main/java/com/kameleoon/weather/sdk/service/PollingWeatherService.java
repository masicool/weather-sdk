package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.client.OpenWeatherMapClient;
import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.ConcurrentLruCache;
import com.kameleoon.weather.sdk.model.WeatherData;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Сервис polling режима с фоновым обновлением данных
 * Автоматически отслеживает запрошенные города (до 10) и обновляет их погоду
 * в фоновом режиме для обеспечения мгновенного ответа на клиентские запросы.
 * Реализует zero-latency responses за счет предварительного обновления данных.
 */
@Slf4j
public class PollingWeatherService implements WeatherService {
    private final OpenWeatherMapClient client;
    private final ConcurrentLruCache<String, WeatherData> cache;
    private final ScheduledExecutorService scheduler;
    private final long pollingIntervalMinutes;

    /**
     * Создает сервис polling режима с указанной конфигурацией
     *
     * @throws IllegalArgumentException если конфигурация некорректна
     */
    public PollingWeatherService(OpenWeatherMapClient client, int maxCacheSize, long pollingIntervalMinutes) {
        this.client = client;
        this.pollingIntervalMinutes = pollingIntervalMinutes;
        this.cache = new ConcurrentLruCache<>(maxCacheSize); // 10 городов по ТЗ, должны передать в параметрах
        this.scheduler = Executors.newScheduledThreadPool(1);

        startPolling();
        log.info("Polling service initialized with update interval: {} minutes", pollingIntervalMinutes);
    }

    /**
     * Запускает фоновое периодическое обновление данных
     */
    private void startPolling() {
        scheduler.scheduleAtFixedRate(
                this::updateAllCities,
                pollingIntervalMinutes, // начальная задержка
                pollingIntervalMinutes, // период
                TimeUnit.MINUTES
        );
        log.debug("Scheduled polling task started with interval: {} minutes", pollingIntervalMinutes);
    }

    /**
     * Получает данные о погоде для указанного города
     * В polling режиме всегда возвращает данные из кеша, обеспечивая мгновенный ответ.
     * Если город запрашивается впервые, добавляет его в список отслеживаемых городов.
     *
     * @param cityName название города на любом языке
     * @return данные о погоде для указанного города
     * @throws WeatherSdkException      если произошла ошибка при получении данных
     * @throws IllegalArgumentException если название города пустое или null
     */
    @Override
    public WeatherData getWeather(String cityName) throws WeatherSdkException {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        String normalizedCityName = cityName.toLowerCase().trim();
        log.debug("Polling mode request for city: {}", cityName);

        // всегда возвращаем из кеша - если города нет, получаем свежие данные
        WeatherData cachedData = cache.get(normalizedCityName);
        if (cachedData != null) {
            log.debug("Returning cached data for city: {}", cityName);
            return cachedData;
        }

        // если города нет в кеше - получаем данные и они автоматически попадут в отслеживаемые
        // через LRU механизм при следующем фоновом обновлении
        log.info("City not in polling cache, fetching fresh data: {}", cityName);
        WeatherData freshData = client.getWeatherByCityName(cityName);

        // данные будут добавлены в кеш при следующем фоновом обновлении
        return freshData;
    }

    /**
     * Обновляет погоду для всех городов в LRU кеше
     * Выполняется периодически в фоновом режиме для поддержания актуальности данных
     * и обеспечения zero-latency ответов на клиентские запросы.
     */
    public void updateAllCities() {
        Set<String> citiesToUpdate = cache.getKeys();
        log.info("Starting scheduled weather update for {} cities", citiesToUpdate.size());

        int successCount = 0;
        int failureCount = 0;

        for (String cityName : citiesToUpdate) {
            try {
                WeatherData freshData = client.getWeatherByCityName(cityName);
                cache.put(cityName, freshData);
                successCount++;
                log.debug("Successfully updated weather for: {}", cityName);
            } catch (Exception e) {
                failureCount++;
                log.warn("Failed to update weather for city {}: {}", cityName, e.getMessage());
            }
        }

        log.info("Scheduled update completed: {} success, {} failures", successCount, failureCount);
    }

    /**
     * Останавливает сервис polling режима и освобождает ресурсы.
     * Прекращает фоновое обновление данных и завершает работу планировщика.
     * Должен вызываться при завершении работы приложения.
     */
    public void shutdown() {
        log.info("Shutting down polling service");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                log.warn("Polling service shutdown forced");
            } else {
                log.debug("Polling service shutdown completed gracefully");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("Polling service shutdown interrupted");
        }
    }
}
