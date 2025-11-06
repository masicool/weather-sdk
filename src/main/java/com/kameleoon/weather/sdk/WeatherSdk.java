package com.kameleoon.weather.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;
import com.kameleoon.weather.sdk.model.WeatherData;
import com.kameleoon.weather.sdk.service.CachingWeatherService;
import com.kameleoon.weather.sdk.service.OnDemandWeatherService;
import com.kameleoon.weather.sdk.service.PollingWeatherService;
import com.kameleoon.weather.sdk.service.WeatherService;
import lombok.extern.slf4j.Slf4j;

/**
 * Основной класс SDK для работы с погодным API OpenWeatherMap
 * Предоставляет унифицированный интерфейс для получения данных о погоде
 * с поддержкой различных режимов работы. Является точкой входа для клиентов SDK.
 * Инкапсулирует логику выбора и настройки сервисов в зависимости от режима работы.
 */
@Slf4j
public class WeatherSdk {
    private final WeatherService weatherService;

    /**
     * Создает экземпляр SDK с указанной конфигурацией
     * Инициализирует соответствующий сервис погоды в зависимости от выбранного режима.
     * Для on-demand режима добавляет кеширующий декоратор.
     *
     * @param config конфигурация SDK (режим работы, настройки кеша, API параметры)
     * @throws IllegalArgumentException если конфигурация некорректна
     */
    WeatherSdk(SdkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SDK configuration cannot be null");
        }

        this.weatherService = createWeatherService(config);
        log.info("Weather SDK initialized with mode: {}", config.getMode());
    }

    /**
     * Создает сервис погоды в зависимости от выбранного режима работы
     * Для polling режима создает PollingWeatherService с фоновым обновлением.
     * Для on-demand режима создает OnDemandWeatherService с кеширующим декоратором.
     *
     * @param config конфигурация SDK
     * @return настроенный сервис погоды
     */
    private WeatherService createWeatherService(SdkConfig config) {
        WeatherService baseService;

        // выбор режима запуска согласно ТЗ
        if (config.getMode() == SdkMode.POLLING) {
            log.debug("Creating PollingWeatherService");
            baseService = new PollingWeatherService(config);
        } else {
            log.debug("Creating OnDemandWeatherService");
            baseService = new OnDemandWeatherService(config);
        }

        // оборачиваем в кеширующий декоратор (для on-demand режима)
        if (config.getMode() == SdkMode.ON_DEMAND) {
            log.debug("Wrapping with CachingWeatherService: TTL={}min, maxSize={}",
                    config.getCacheTtlMinutes(), config.getMaxCacheSize());
            return new CachingWeatherService(
                    baseService,
                    config.getCacheTtlMinutes(),
                    config.getMaxCacheSize()
            );
        }

        // в polling режиме кеширование не нужно - там свой кеш
        log.debug("Using base service without caching for polling mode");
        return baseService;
    }

    /**
     * Получает данные о погоде для указанного города в формате JSON
     * Основной метод SDK для клиентов. Делегирует запрос соответствующему сервису
     * и преобразует результат в JSON строку согласно формату ТЗ.
     *
     * @param cityName название города на любом языке
     * @return JSON строка с данными о погоде в формате указанном в ТЗ
     * @throws Exception                если произошла ошибка при получении или преобразовании данных,
     *                                  город не найден или превышены лимиты API
     * @throws IllegalArgumentException если название города пустое или null
     */
    public String getWeather(String cityName) throws Exception {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        log.debug("Processing weather request for city: {}", cityName);
        WeatherData data = weatherService.getWeather(cityName);

        String jsonResult = convertToOutputJson(data);
        log.debug("Weather data successfully processed for city: {}", cityName);

        return jsonResult;
    }

    /**
     * Останавливает работу SDK и освобождает ресурсы
     * Особенно важно для polling режима - останавливает фоновые потоки.
     * Для on-demand режима не требует специальных действий.
     * Должен вызываться при завершении работы приложения.
     */
    public void shutdown() {
        if (weatherService instanceof PollingWeatherService) {
            log.info("Shutting down polling service");
            ((PollingWeatherService) weatherService).shutdown();
        } else {
            log.debug("No shutdown required for on-demand service");
        }
    }

    /**
     * Преобразует объект WeatherData в JSON строку требуемого формата
     * Использует Jackson ObjectMapper для сериализации объекта в JSON.
     * Формат выходных данных соответствует техническому заданию.
     *
     * @param data объект с данными о погоде
     * @return JSON строка в формате ТЗ
     * @throws Exception если произошла ошибка при сериализации
     */
    private String convertToOutputJson(WeatherData data) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }
}
