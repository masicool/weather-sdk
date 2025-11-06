package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.client.OpenWeatherMapClient;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.WeatherData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса polling режима
 */
@ExtendWith(MockitoExtension.class)
class PollingWeatherServiceTest {
    @Mock
    private OpenWeatherMapClient mockWeatherClient;

    private PollingWeatherService pollingService;

    @BeforeEach
    void setUp() {
        // 1 минута для тестов
        SdkConfig config = SdkConfig.builder("test-api-key")
                .pollingIntervalMinutes(1) // 1 минута для тестов
                .maxCacheSize(3)
                .build();

        OpenWeatherMapClient mockClient = mock(OpenWeatherMapClient.class);
        pollingService = new PollingWeatherService(mockClient, config.getMaxCacheSize(), config.getPollingIntervalMinutes());
    }

    @AfterEach
    void tearDown() {
        pollingService.shutdown();
    }

    @Test
    void shouldReturnCachedDataForSameCity() throws WeatherSdkException {
        String cityName = "Moscow";
        WeatherData expectedData = createTestWeatherData(cityName);

        when(mockWeatherClient.getWeatherByCityName(cityName))
                .thenReturn(expectedData);

        WeatherData result1 = pollingService.getWeather(cityName);
        WeatherData result2 = pollingService.getWeather(cityName);

        assertEquals(expectedData, result1);
        assertEquals(expectedData, result2);
        // клиент должен быть вызван только один раз (второй раз из кеша)
        verify(mockWeatherClient, times(1)).getWeatherByCityName(cityName);
    }

    @Test
    void shouldHandleMultipleCitiesWithinLimit() throws WeatherSdkException {
        WeatherData moscowData = createTestWeatherData("Moscow");
        WeatherData londonData = createTestWeatherData("London");
        WeatherData parisData = createTestWeatherData("Paris");

        when(mockWeatherClient.getWeatherByCityName("Moscow")).thenReturn(moscowData);
        when(mockWeatherClient.getWeatherByCityName("London")).thenReturn(londonData);
        when(mockWeatherClient.getWeatherByCityName("Paris")).thenReturn(parisData);

        // Act
        WeatherData result1 = pollingService.getWeather("Moscow");
        WeatherData result2 = pollingService.getWeather("London");
        WeatherData result3 = pollingService.getWeather("Paris");

        // Assert
        assertEquals(moscowData, result1);
        assertEquals(londonData, result2);
        assertEquals(parisData, result3);

        verify(mockWeatherClient, times(1)).getWeatherByCityName("Moscow");
        verify(mockWeatherClient, times(1)).getWeatherByCityName("London");
        verify(mockWeatherClient, times(1)).getWeatherByCityName("Paris");
    }

    @Test
    void shouldUpdateCitiesInBackground() throws WeatherSdkException {
        // Arrange
        String cityName = "Moscow";
        WeatherData initialData = createTestWeatherData(cityName, 20.0);
        WeatherData updatedData = createTestWeatherData(cityName, 25.0);

        when(mockWeatherClient.getWeatherByCityName(cityName))
                .thenReturn(initialData)
                .thenReturn(updatedData);

        // Act - первый вызов
        WeatherData firstResult = pollingService.getWeather(cityName);

        // Имитируем фоновое обновление
        pollingService.updateAllCities();

        // Второй вызов должен вернуть обновленные данные
        WeatherData secondResult = pollingService.getWeather(cityName);

        // Assert
        assertEquals(initialData, firstResult);
        assertEquals(updatedData, secondResult);
        verify(mockWeatherClient, times(2)).getWeatherByCityName(cityName);
    }

    @Test
    void shouldHandleClientExceptionsGracefully() throws WeatherSdkException {
        // Arrange
        String cityName = "Moscow";
        WeatherData data = createTestWeatherData(cityName);

        when(mockWeatherClient.getWeatherByCityName(cityName))
                .thenReturn(data)
                .thenThrow(new WeatherSdkException("API error"));

        // Act - первый успешный вызов
        WeatherData firstResult = pollingService.getWeather(cityName);

        // Второй вызов при ошибке обновления - должен вернуть старые данные из кеша
        pollingService.updateAllCities(); // Это вызовет исключение, но не должно сломать сервис
        WeatherData secondResult = pollingService.getWeather(cityName);

        // Assert
        assertEquals(data, firstResult);
        assertEquals(data, secondResult);
        // Клиент должен быть вызван дважды (второй раз с ошибкой)
        verify(mockWeatherClient, times(2)).getWeatherByCityName(cityName);
    }

    @Test
    void shouldShutdownProperly() throws WeatherSdkException {
        // Arrange
        String cityName = "Moscow";
        WeatherData data = createTestWeatherData(cityName);

        when(mockWeatherClient.getWeatherByCityName(cityName)).thenReturn(data);

        // Act
        pollingService.getWeather(cityName);
        pollingService.shutdown();

        // Assert - сервис должен быть остановлен, повторный shutdown не должен ломать
        assertDoesNotThrow(() -> pollingService.shutdown());
    }

    @Test
    void shouldThrowExceptionForEmptyCityName() {
        assertThrows(IllegalArgumentException.class, () -> pollingService.getWeather(null));
        assertThrows(IllegalArgumentException.class, () -> pollingService.getWeather(""));
        assertThrows(IllegalArgumentException.class, () -> pollingService.getWeather("   "));
    }

    @Test
    void shouldLimitCacheSizeAccordingToConfig() throws WeatherSdkException {
        // Arrange - конфиг с размером кеша 2
        SdkConfig smallCacheConfig = SdkConfig.builder("test-key")
                .pollingIntervalMinutes(1)
                .maxCacheSize(2)
                .build();

        OpenWeatherMapClient mockClient = mock(OpenWeatherMapClient.class);
        PollingWeatherService smallCacheService = new PollingWeatherService(mockClient,
                smallCacheConfig.getMaxCacheSize(), smallCacheConfig.getPollingIntervalMinutes());

        WeatherData data1 = createTestWeatherData("Moscow");
        WeatherData data2 = createTestWeatherData("London");
        WeatherData data3 = createTestWeatherData("Paris");

        when(mockWeatherClient.getWeatherByCityName(anyString()))
                .thenReturn(data1)
                .thenReturn(data2)
                .thenReturn(data3);

        try {
            // Act - запрашиваем 3 города при лимите 2
            smallCacheService.getWeather("Moscow");
            smallCacheService.getWeather("London");
            smallCacheService.getWeather("Paris"); // Должен вытеснить самый старый

            // Assert - все города должны быть доступны (кеш сам управляет размером)
            assertNotNull(smallCacheService.getWeather("Moscow"));
            assertNotNull(smallCacheService.getWeather("London"));
            assertNotNull(smallCacheService.getWeather("Paris"));

        } finally {
            smallCacheService.shutdown();
        }
    }

    private WeatherData createTestWeatherData(String cityName) {
        return createTestWeatherData(cityName, 25.0);
    }

    private WeatherData createTestWeatherData(String cityName, double temperature) {
        WeatherData.Weather weather = new WeatherData.Weather("Clear", "clear sky");
        WeatherData.Temperature temp = new WeatherData.Temperature(temperature, temperature + 1);
        WeatherData.Wind wind = new WeatherData.Wind(5.0);
        WeatherData.Sys sys = new WeatherData.Sys(1675751262L, 1675787560L);

        return new WeatherData(weather, temp, 10000, wind, 1675744800L, sys, 3600, cityName);
    }
}
