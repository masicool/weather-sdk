package com.kameleoon.weather.sdk.client;

import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.exception.ApiException;
import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты для клиента OpenWeatherMap с моками HTTP
 */
class OpenWeatherMapClientTest {

    @Mock
    private HttpClient mockHttpClient;

    private OpenWeatherMapClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SdkConfig config = new SdkConfig("test-api-key");
        client = new OpenWeatherMapClient(config, mockHttpClient);
    }

    @Test
    void getWeather_ShouldReturnWeatherData_WhenValidResponse() throws Exception {
        String jsonResponse = """
                {
                    "weather": [{"main": "Clouds", "description": "scattered clouds"}],
                    "main": {"temp": 269.6, "feels_like": 267.57},
                    "visibility": 10000,
                    "wind": {"speed": 1.38},
                    "dt": 1675744800,
                    "sys": {"sunrise": 1675751262, "sunset": 1675787560},
                    "timezone": 3600,
                    "name": "Zocca"
                }
                """;

        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(200, jsonResponse));

        WeatherData weather = client.getWeatherByCityName("London");

        assertNotNull(weather);
        assertEquals("Zocca", weather.getName());
        assertEquals(269.6, weather.getTemperature().getTemp());
        assertEquals(267.57, weather.getTemperature().getFeelsLike());
        assertEquals(10000, weather.getVisibility());
        assertEquals(1.38, weather.getWind().getSpeed());
        assertEquals(1675744800, weather.getDatetime());
        assertEquals(1675751262, weather.getSys().getSunrise());
        assertEquals(1675787560, weather.getSys().getSunset());
        assertEquals(3600, weather.getTimezone());
    }

    @Test
    void getWeather_ShouldThrowException_WhenHttpError() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new IOException("Network error"));

        WeatherSdkException exception = assertThrows(WeatherSdkException.class,
                () -> client.getWeatherByCityName("London"));

        assertTrue(exception.getMessage().contains("Network communication error"));
    }

    @Test
    void getWeather_ShouldThrowException_WhenApiReturnsError() throws Exception {
        String errorResponse = "{\"cod\":401, \"message\": \"Invalid API key\"}";
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(401, errorResponse));

        ApiException exception = assertThrows(ApiException.class,
                () -> client.getWeatherByCityName("London"));

        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void getWeather_ShouldThrowException_WhenInvalidJson() throws Exception {
        String invalidJson = "invalid json";
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(400, invalidJson));

        WeatherSdkException exception = assertThrows(WeatherSdkException.class,
                () -> client.getWeatherByCityName("London"));

        assertTrue(exception.getMessage().contains("Invalid request parameters"));
    }

    @Test
    void getWeather_ShouldThrowException_WhenCityNotFound() throws Exception {
        String notFoundResponse = "{\"cod\":\"404\", \"message\":\"city not found\"}";
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(404, notFoundResponse));

        ApiException exception = assertThrows(ApiException.class,
                () -> client.getWeatherByCityName("InvalidCity"));

        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void getWeather_ShouldHandleEmptyResponse() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(200, ""));

        WeatherSdkException exception = assertThrows(WeatherSdkException.class,
                () -> client.getWeatherByCityName("London"));

        assertTrue(exception.getMessage().contains("Network communication error"));
    }

    @Test
    void getWeather_ShouldUseCorrectUrlFormat() throws Exception {
        String jsonResponse = """
                {
                    "weather": [{"main": "Clear", "description": "clear sky"}],
                    "main": {"temp": 280.0, "feels_like": 278.0},
                    "visibility": 10000,
                    "wind": {"speed": 2.0},
                    "dt": 1675744800,
                    "sys": {"sunrise": 1675751262, "sunset": 1675787560},
                    "timezone": 3600,
                    "name": "London"
                }
                """;

        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenReturn(createMockResponse(200, jsonResponse));

        WeatherData weather = client.getWeatherByCityName("London");

        assertNotNull(weather);
        assertEquals("London", weather.getName());

        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    /**
     * Создает mock HttpResponse для тестов
     */
    private HttpResponse<Object> createMockResponse(int statusCode, String body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public HttpRequest request() {
                return HttpRequest.newBuilder()
                        .uri(URI.create("https://api.openweathermap.org/data/2.5/weather"))
                        .build();
            }

            @Override
            public Optional<HttpResponse<Object>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(Collections.emptyMap(), (s1, s2) -> true);
            }

            @Override
            public URI uri() {
                return URI.create("https://api.openweathermap.org/data/2.5/weather");
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }
}
