package com.kameleoon.weather.sdk.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.exception.*;
import com.kameleoon.weather.sdk.model.ApiWeatherResponse;
import com.kameleoon.weather.sdk.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для клиента OpenWeatherMap с моками HTTP
 */
@ExtendWith(MockitoExtension.class)
class OpenWeatherMapClientTest {
    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private OpenWeatherMapClient weatherClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SdkConfig config = SdkConfig.builder("a28a880098176a505a802319e9e6ee66")
                .weatherApiUrl("https://api.openweathermap.org/data/2.5/weather")
                .timeoutSeconds(30)
                .build();

        objectMapper = new ObjectMapper();

        // Создаем реальный клиент, но подменим HTTP клиент через рефлексию
        // В реальном проекте лучше использовать конструктор, принимающий HttpClient
        weatherClient = new OpenWeatherMapClient(config);
    }

    @Test
    void shouldReturnWeatherDataForValidCity() throws Exception {
        // Arrange
        String cityName = "Moscow";
        String apiResponseJson = createSuccessfulApiResponse(cityName);

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(apiResponseJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act
        WeatherData result = weatherClient.getWeatherByCityName(cityName);

        // Assert
        assertNotNull(result);
        assertEquals(cityName, result.getName());
        assertEquals(25.0, result.getTemperature().getTemp());
        assertEquals("Clear", result.getWeather().getMain());
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @Test
    void shouldThrowCityNotFoundExceptionFor404() throws Exception {
        // Arrange
        String cityName = "UnknownCity";
        String errorResponse = "{\"cod\":\"404\",\"message\":\"city not found\"}";

        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpResponse.body()).thenReturn(errorResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act & Assert
        CityNotFoundException exception = assertThrows(CityNotFoundException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("Город не найден: " + cityName, exception.getMessage());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void shouldThrowInvalidApiKeyExceptionFor401() throws Exception {
        // Arrange
        String cityName = "Moscow";
        String errorResponse = "{\"cod\":401, \"message\": \"Invalid API key\"}";

        when(mockHttpResponse.statusCode()).thenReturn(401);
        when(mockHttpResponse.body()).thenReturn(errorResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act & Assert
        InvalidApiKeyException exception = assertThrows(InvalidApiKeyException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("Неверный API ключ. Проверьте правильность ключа.", exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void shouldThrowAccessDeniedExceptionFor429() throws Exception {
        // Arrange
        String cityName = "Moscow";
        String errorResponse = "{\"cod\":429, \"message\": \"Rate limit exceeded\"}";

        when(mockHttpResponse.statusCode()).thenReturn(429);
        when(mockHttpResponse.body()).thenReturn(errorResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("Превышен лимит запросов. Попробуйте позже.", exception.getMessage());
        assertEquals(429, exception.getStatusCode());
    }

    @Test
    void shouldThrowApiExceptionForServerError() throws Exception {
        // Arrange
        String cityName = "Moscow";

        when(mockHttpResponse.statusCode()).thenReturn(500);
        when(mockHttpResponse.body()).thenReturn("Server error");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("OpenWeatherMap server error. Please try again later.", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void shouldThrowWeatherSdkExceptionForNetworkError() throws Exception {
        // Arrange
        String cityName = "Moscow";

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.net.ConnectException("Connection refused"));

        // Act & Assert
        WeatherSdkException exception = assertThrows(WeatherSdkException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("Network connection error", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForEmptyCityName() {
        assertThrows(IllegalArgumentException.class, () -> weatherClient.getWeatherByCityName(null));
        assertThrows(IllegalArgumentException.class, () -> weatherClient.getWeatherByCityName(""));
        assertThrows(IllegalArgumentException.class, () -> weatherClient.getWeatherByCityName("   "));
    }

    @Test
    void shouldHandleMalformedJsonResponse() throws Exception {
        // Arrange
        String cityName = "Moscow";

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("invalid json");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        // Act & Assert
        WeatherSdkException exception = assertThrows(WeatherSdkException.class,
                () -> weatherClient.getWeatherByCityName(cityName));

        assertEquals("Failed to process weather data", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    private String createSuccessfulApiResponse(String cityName) throws JsonProcessingException {
        ApiWeatherResponse response = new ApiWeatherResponse();
        response.setName(cityName);
        response.setTimezone(10800);
        response.setDt(1675744800L);
        response.setVisibility(10000);

        ApiWeatherResponse.Weather weather = new ApiWeatherResponse.Weather();
        weather.setMain("Clear");
        weather.setDescription("clear sky");
        response.setWeather(java.util.List.of(weather));

        ApiWeatherResponse.Main main = new ApiWeatherResponse.Main();
        main.setTemp(25.0);
        main.setFeelsLike(26.0);
        response.setMain(main);

        ApiWeatherResponse.Wind wind = new ApiWeatherResponse.Wind();
        wind.setSpeed(5.0);
        response.setWind(wind);

        ApiWeatherResponse.Sys sys = new ApiWeatherResponse.Sys();
        sys.setSunrise(1675751262L);
        sys.setSunset(1675787560L);
        response.setSys(sys);

        return objectMapper.writeValueAsString(response);
    }
}
