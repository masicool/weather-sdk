package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.ConcurrentLruCache;
import com.kameleoon.weather.sdk.model.WeatherData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Декоратор сервиса погоды с потокобезопасным LRU кешированием
 * Обеспечивает кеширование данных о погоде с автоматическим удалением
 * наименее используемых городов при превышении лимита. Гарантирует
 * корректную работу в многопоточной среде.
 */
@Slf4j
public class CachingWeatherService implements WeatherService {

    private final WeatherService wrappedService;
    private final ConcurrentLruCache<String, CacheEntry> cache;
    private final long ttlMillis;

    /**
     * Создает сервис кеширования с указанными параметрами
     *
     * @param wrappedService базовый сервис погоды для делегирования запросов
     * @param ttlMinutes     время жизни кешированных данных в минутах
     * @param maxSize        максимальное количество городов в кеше
     * @throws IllegalArgumentException если ttlMinutes или maxSize некорректны
     */
    public CachingWeatherService(WeatherService wrappedService, long ttlMinutes, int maxSize) {
        if (wrappedService == null) {
            throw new IllegalArgumentException("Wrapped weather service cannot be null");
        }
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException("Cache TTL must be positive");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache max size must be positive");
        }

        this.wrappedService = wrappedService;
        this.ttlMillis = ttlMinutes * 60 * 1000;
        this.cache = new ConcurrentLruCache<>(maxSize);

        log.debug("Caching service initialized: TTL={} minutes, maxSize={}", ttlMinutes, maxSize);
    }

    /**
     * Получает данные о погоде для указанного города с использованием кеширования
     * При первом запросе города данные получаются из базового сервиса и кешируются.
     * При повторных запросах в течение времени жизни кеша данные возвращаются из кеша.
     * Автоматически управляет размером кеша по алгоритму LRU.
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
        log.debug("Weather request for city: {}", cityName);

        // пытаемся получить данные из кэша
        CacheEntry cachedEntry = cache.get(normalizedCityName);
        if (cachedEntry != null && isCacheEntryValid(cachedEntry)) {
            log.debug("Cache hit for city: {}", cityName);
            return cachedEntry.getData();
        }

        // обновляем данные, если их нет в кэше или они уже не актуальны
        log.debug("Cache miss for city: {}", cityName);
        WeatherData freshData = wrappedService.getWeather(cityName);

        // сохраняем актуальные данные в кэш
        CacheEntry newEntry = new CacheEntry(freshData, System.currentTimeMillis());
        cache.put(normalizedCityName, newEntry);
        log.debug("Data cached for city: {}", cityName);

        return freshData;
    }

    /**
     * Проверяет актуальность кешированной записи
     *
     * @param entry запись кеша для проверки
     * @return true если запись еще актуальна, false если устарела
     */
    private boolean isCacheEntryValid(CacheEntry entry) {
        long currentTime = System.currentTimeMillis();
        long entryAge = currentTime - entry.getTimestamp();
        boolean isValid = entryAge < ttlMillis;

        if (!isValid) {
            log.debug("Cache entry expired, age: {} ms", entryAge);
        }

        return isValid;
    }

    /**
     * Запись кеша, содержащая данные о погоде и время их получения
     */
    @Getter
    private static class CacheEntry {
        private final WeatherData data;
        private final long timestamp;

        /**
         * Создает новую запись кеша
         *
         * @param data      данные о погоде
         * @param timestamp время получения данных в миллисекундах
         */
        public CacheEntry(WeatherData data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

    }
}
