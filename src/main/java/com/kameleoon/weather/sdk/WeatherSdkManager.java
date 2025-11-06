package com.kameleoon.weather.sdk;

import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления экземплярами WeatherSdk (паттерн Мультитон)
 * Обеспечивает создание и управление экземплярами SDK по API ключам.
 * Гарантирует, что для каждого API ключа создается только один экземпляр SDK.
 * Реализует потокобезопасное создание и уничтожение экземпляров.
 */
@Slf4j
public class WeatherSdkManager {
    private static WeatherSdkManager instance;
    private final Map<String, WeatherSdk> sdkInstances = new ConcurrentHashMap<>();

    /**
     * Приватный конструктор для реализации паттерна Singleton
     */
    private WeatherSdkManager() {
        log.debug("WeatherSdkManager initialized");
    }

    /**
     * Возвращает единственный экземпляр менеджера SDK (паттерн Singleton)
     * Гарантирует создание только одного экземпляра менеджера в приложении.
     * Потокобезопасная реализация с двойной проверкой блокировки.
     *
     * @return единственный экземпляр WeatherSdkManager
     */
    public static synchronized WeatherSdkManager getInstance() {
        if (instance == null) {
            instance = new WeatherSdkManager();
            log.info("WeatherSdkManager instance created");
        }
        return instance;
    }

    /**
     * Возвращает экземпляр SDK для указанной конфигурации (основной метод)
     * Создает новый экземпляр SDK если его нет, или возвращает существующий.
     * Ключом для хранения является комбинация API ключа и режима работы.
     *
     * @param config конфигурация SDK
     * @return экземпляр WeatherSdk для указанной конфигурации
     * @throws IllegalArgumentException если конфигурация некорректна
     */
    public WeatherSdk getSdk(SdkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SDK configuration cannot be null");
        }

        String key = generateInstanceKey(config);
        log.debug("Requesting SDK instance for key: {}", maskApiKey(key));

        return sdkInstances.computeIfAbsent(key, k -> {
            log.info("Creating new SDK instance for API key: {}, mode: {}", maskApiKey(config.getApiKey()), config.getMode());
            return new WeatherSdk(config);
        });
    }

    /**
     * Возвращает экземпляр SDK для указанного API ключа и режима работы
     * Упрощенный метод для быстрого создания SDK с минимальной конфигурацией.
     * Остальные параметры берутся из значений по умолчанию.
     *
     * @param apiKey API ключ OpenWeatherMap
     * @param mode   режим работы SDK
     * @return экземпляр WeatherSdk
     * @throws IllegalArgumentException если API ключ пустой или null
     */
    public WeatherSdk getSdk(String apiKey, SdkMode mode) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }

        SdkConfig config = SdkConfig.builder(apiKey).mode(mode).build();
        return getSdk(config);
    }

    /**
     * Возвращает экземпляр SDK для указанного API ключа в режиме on-demand
     * Наиболее простой способ создания SDK с API ключом и настройками по умолчанию.
     *
     * @param apiKey API ключ OpenWeatherMap
     * @return экземпляр WeatherSdk в режиме on-demand
     * @throws IllegalArgumentException если API ключ пустой или null
     */
    public WeatherSdk getSdk(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }

        return getSdk(new SdkConfig(apiKey));
    }

    /**
     * Возвращает экземпляр SDK с API ключом из переменной окружения
     * Автоматически загружает API ключ из переменной окружения OPENWEATHER_API_KEY.
     * Удобно для production окружений где ключи хранятся в environment variables.
     *
     * @return экземпляр WeatherSdk
     * @throws IllegalStateException если переменная окружения OPENWEATHER_API_KEY не установлена
     */
    public WeatherSdk getSdk() {
        String apiKey = System.getenv("OPENWEATHER_API_KEY");
        if (apiKey == null) {
            log.error("OPENWEATHER_API_KEY environment variable is not set");
            throw new IllegalStateException("OPENWEATHER_API_KEY environment variable is not set");
        }

        log.debug("Using API key from environment variable");
        return getSdk(apiKey);
    }

    /**
     * Генерирует уникальный ключ для хранения экземпляров SDK
     * Ключ формируется как комбинация API ключа и режима работы.
     * Это позволяет иметь разные экземпляры SDK для одного API ключа
     * но с разными режимами работы.
     *
     * @param config конфигурация SDK
     * @return уникальный ключ для хранения экземпляра
     */
    private String generateInstanceKey(SdkConfig config) {
        return config.getApiKey() + "|" + config.getMode().getValue();
    }

    /**
     * Уничтожает экземпляр SDK для указанной конфигурации
     * Удаляет экземпляр из кеша менеджера и освобождает ресурсы.
     * Особенно важно для polling режима для остановки фоновых потоков.
     *
     * @param config конфигурация SDK
     */
    public void destroySdk(SdkConfig config) {
        if (config == null) {
            log.warn("Attempt to destroy SDK with null configuration");
            return;
        }

        String key = generateInstanceKey(config);
        destroySdk(key);
    }

    /**
     * Уничтожает экземпляр SDK для указанного API ключа и режима
     *
     * @param apiKey API ключ OpenWeatherMap
     * @param mode   режим работы SDK
     */
    public void destroySdk(String apiKey, SdkMode mode) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Attempt to destroy SDK with empty API key");
            return;
        }

        String key = apiKey + "|" + mode.getValue();
        destroySdk(key);
    }

    /**
     * Уничтожает экземпляр SDK по ключу и освобождает ресурсы
     *
     * @param instanceKey ключ экземпляра SDK
     */
    private void destroySdk(String instanceKey) {
        WeatherSdk sdk = sdkInstances.remove(instanceKey);
        if (sdk != null) {
            log.info("Destroying SDK instance for key: {}", maskApiKey(instanceKey));
            sdk.shutdown();
        } else {
            log.debug("SDK instance not found for key: {}", maskApiKey(instanceKey));
        }
    }

    /**
     * Маскирует API ключ в логах для безопасности
     *
     * @param apiKey исходный API ключ или ключ экземпляра
     * @return замаскированная версия ключа
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
