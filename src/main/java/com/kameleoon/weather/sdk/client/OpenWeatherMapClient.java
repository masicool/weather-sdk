package com.kameleoon.weather.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.exception.*;
import com.kameleoon.weather.sdk.model.ApiWeatherResponse;
import com.kameleoon.weather.sdk.model.WeatherData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Клиент для работы с OpenWeatherMap Current Weather API
 * Выполняет HTTP запросы к API и преобразует ответы в модели SDK.
 * Обрабатывает ошибки API и сетевые проблемы, выбрасывая соответствующие исключения.
 */
@Slf4j
public class OpenWeatherMapClient {
    @Getter
    private final SdkConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Создает клиент для работы с OpenWeatherMap API
     *
     * @param config конфигурация SDK с настройками подключения
     * @throws IllegalArgumentException если конфигурация некорректна
     */
    public OpenWeatherMapClient(SdkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SDK configuration cannot be null");
        }

        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();

        log.debug("OpenWeatherMap client initialized with timeout: {} seconds", config.getTimeoutSeconds());
    }

    /**
     * Создает клиент для работы с OpenWeatherMap API (используется для тестов)
     *
     * @param config     конфигурация SDK с настройками подключения
     * @param httpClient HTTP-клиент
     */
    public OpenWeatherMapClient(SdkConfig config, HttpClient httpClient) {
        if (config == null) {
            throw new IllegalArgumentException("SDK configuration cannot be null");
        }

        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = httpClient;

        log.debug("OpenWeatherMap client initialized with timeout: {} seconds", config.getTimeoutSeconds());
    }

    /**
     * Получает данные о погоде для указанного города.
     * Выполняет запрос к Current Weather API 2.5 и преобразует ответ
     * в клиентскую модель WeatherData.
     *
     * @param cityName название города на любом языке
     * @return данные о погоде в формате клиентской модели
     * @throws WeatherSdkException      если произошла ошибка API или сети
     * @throws IllegalArgumentException если название города пустое
     */
    public WeatherData getWeatherByCityName(String cityName) throws WeatherSdkException {
        log.debug("Initiating weather API request for city: {}", cityName);

        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        String url = String.format("%s?q=%s&appid=%s&units=metric",
                config.getWeatherApiUrl(), cityName.trim(), config.getApiKey());

        long startTime = System.currentTimeMillis();

        try {
            HttpRequest request = createRequest(url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long duration = System.currentTimeMillis() - startTime;
            log.debug("API request completed in {} ms for city: {}", duration, cityName);

            // обрабатываем HTTP статусы
            handleHttpStatus(response.statusCode(), cityName, response.body());

            // парсим успешный ответ в API модель
            ApiWeatherResponse apiResponse = objectMapper.readValue(response.body(), ApiWeatherResponse.class);
            log.info("Weather data successfully retrieved for city: {}", cityName);

            // преобразуем API модель в клиентскую модель
            return mapToWeatherData(apiResponse);

        } catch (WeatherSdkException e) {
            // перебрасываем наши кастомные исключения
            throw e;
        } catch (java.net.ConnectException e) {
            log.error("Network connection failed for city: {} - {}", cityName, e.getMessage());
            throw new WeatherSdkException("Network connection error", e);
        } catch (java.net.UnknownHostException e) {
            log.error("Unknown host for API request: {} - {}", cityName, e.getMessage());
            throw new WeatherSdkException("API server unavailable", e);
        } catch (java.net.http.HttpTimeoutException e) {
            log.warn("API request timeout for city: {}", cityName);
            throw new WeatherSdkException("API request timeout", e);
        } catch (java.io.IOException e) {
            log.error("I/O error during API request for city: {} - {}", cityName, e.getMessage());
            throw new WeatherSdkException("Network communication error", e);
        } catch (Exception e) {
            log.error("Unexpected error during API request for city: {} - {}", cityName, e.getMessage());
            throw new WeatherSdkException("Unexpected error while fetching weather data", e);
        }
    }

    /**
     * Преобразует API модель в клиентскую модель WeatherData
     *
     * @param apiResponse ответ от OpenWeatherMap API
     * @return данные в формате клиентской модели
     * @throws WeatherSdkException если данные не могут быть преобразованы
     */
    private WeatherData mapToWeatherData(ApiWeatherResponse apiResponse) throws WeatherSdkException {
        try {
            // извлекаем первый элемент weather (обычно он один)
            ApiWeatherResponse.Weather apiWeather = apiResponse.getWeather() != null &&
                    !apiResponse.getWeather().isEmpty()
                    ? apiResponse.getWeather().getFirst()
                    : null;

            // создаем клиентскую модель согласно ТЗ
            WeatherData.Weather weather = apiWeather != null
                    ? new WeatherData.Weather(apiWeather.getMain(), apiWeather.getDescription())
                    : null;

            WeatherData.Temperature temperature = apiResponse.getMain() != null
                    ? new WeatherData.Temperature(
                    apiResponse.getMain().getTemp(),
                    apiResponse.getMain().getFeelsLike())
                    : null;

            WeatherData.Wind wind = apiResponse.getWind() != null
                    ? new WeatherData.Wind(apiResponse.getWind().getSpeed())
                    : null;

            WeatherData.Sys sys = apiResponse.getSys() != null
                    ? new WeatherData.Sys(
                    apiResponse.getSys().getSunrise(),
                    apiResponse.getSys().getSunset())
                    : null;

            return new WeatherData(
                    weather,
                    temperature,
                    apiResponse.getVisibility() != null ? apiResponse.getVisibility() : 0,
                    wind,
                    apiResponse.getDt() != null ? apiResponse.getDt() : 0,
                    sys,
                    apiResponse.getTimezone() != null ? apiResponse.getTimezone() : 0,
                    apiResponse.getName() != null ? apiResponse.getName() : ""
            );

        } catch (Exception e) {
            log.error("Failed to transform API response to client model: {}", e.getMessage());
            throw new WeatherSdkException("Failed to process weather data", e);
        }
    }

    /**
     * Обрабатывает HTTP статусы ответа от API
     */
    private void handleHttpStatus(int statusCode, String cityName, String responseBody)
            throws WeatherSdkException {

        try {
            // пытаемся распарсить сообщение об ошибке от API
            String apiMessage = extractApiErrorMessage(responseBody);

            switch (statusCode) {
                case 200 -> {
                    // успешный запрос - ничего не делаем
                }
                case 400 -> {
                    log.warn("Bad request for city: {} - {}", cityName, apiMessage);
                    throw new ApiException(400, apiMessage, "Invalid request parameters");
                }
                case 401 -> {
                    log.error("Invalid API key for city: {} - {}", cityName, apiMessage);
                    throw new InvalidApiKeyException(apiMessage);
                }
                case 404 -> {
                    log.warn("City not found: {} - {}", cityName, apiMessage);
                    throw new CityNotFoundException(cityName, apiMessage);
                }
                case 429 -> {
                    log.warn("Rate limit exceeded for city: {} - {}", cityName, apiMessage);
                    throw new AccessDeniedException(apiMessage);
                }
                case 500, 502, 503, 504 -> {
                    log.error("OpenWeatherMap server error {} for city: {} - {}",
                            statusCode, cityName, apiMessage);
                    throw new ApiException(statusCode, apiMessage, "OpenWeatherMap server error. Please try again later.");
                }
                default -> {
                    log.error("Unexpected API status {} for city: {} - {}",
                            statusCode, cityName, apiMessage);
                    throw new ApiException(statusCode, apiMessage, "Unknown API error: " + statusCode);
                }
            }

        } catch (WeatherSdkException e) {
            throw e; // перебрасываем наши исключения
        } catch (Exception e) {
            // если не удалось распарсить ошибку API
            log.error("Failed to parse API error response for status {}: {}", statusCode, responseBody);
            throw new ApiException(statusCode, responseBody, "API error: " + statusCode + ". Failed to process response.");
        }
    }

    /**
     * Извлекает сообщение об ошибке из тела ответа API
     */
    private String extractApiErrorMessage(String responseBody) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);

            // пытаемся извлечь сообщение об ошибке в разных форматах
            if (jsonNode.has("message")) {
                return jsonNode.get("message").asText();
            }
            if (jsonNode.has("error")) {
                return jsonNode.get("error").asText();
            }
            if (jsonNode.has("cod") && jsonNode.has("message")) {
                return jsonNode.get("message").asText();
            }

            return responseBody; // возвращаем все тело если не нашли структурированное сообщение

        } catch (Exception e) {
            return responseBody; // возвращаем сырой ответ если не JSON
        }
    }

    /**
     * Создает HTTP запрос с настройками таймаута
     */
    private HttpRequest createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .header("Accept", "application/json")
                .header("User-Agent", "WeatherSDK/1.0")
                .GET()
                .build();
    }
}
