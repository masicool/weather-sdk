package com.kameleoon.weather.sdk.service;

import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса кеширования
 */
@ExtendWith(MockitoExtension.class)
class CachingWeatherServiceTest {
    @Mock
    private WeatherService mockWeatherService;

    private CachingWeatherService cachingService;

    @BeforeEach
    void setUp() {
        cachingService = new CachingWeatherService(mockWeatherService, 10, 2);
    }

    @Test
    void shouldCacheWeatherData() throws WeatherSdkException {
        WeatherData expectedData = createTestWeatherData("Moscow");
        when(mockWeatherService.getWeather("Moscow")).thenReturn(expectedData);

        // первый вызов - должен обратиться к wrapped service
        WeatherData result1 = cachingService.getWeather("Moscow");

        // второй вызов - должен вернуть данные из кеша
        WeatherData result2 = cachingService.getWeather("Moscow");

        assertEquals(expectedData, result1);
        assertEquals(expectedData, result2);
        // wrapped service должен быть вызван только один раз
        verify(mockWeatherService, times(1)).getWeather("Moscow");
    }

    @Test
    void shouldEvictCacheWhenCapacityExceeded() throws WeatherSdkException {
        WeatherData data1 = createTestWeatherData("Moscow");
        WeatherData data2 = createTestWeatherData("London");
        WeatherData data3 = createTestWeatherData("Paris");

        when(mockWeatherService.getWeather("Moscow")).thenReturn(data1);
        when(mockWeatherService.getWeather("London")).thenReturn(data2);
        when(mockWeatherService.getWeather("Paris")).thenReturn(data3);

        cachingService.getWeather("Moscow");
        cachingService.getWeather("London");
        cachingService.getWeather("Paris"); // должен вытеснить Moscow

        // повторный запрос Moscow должен снова обратиться к сервису
        cachingService.getWeather("Moscow");

        verify(mockWeatherService, times(2)).getWeather("Moscow");
        verify(mockWeatherService, times(1)).getWeather("London");
        verify(mockWeatherService, times(1)).getWeather("Paris");
    }

    @Test
    void shouldThrowExceptionForEmptyCityName() {
        assertThrows(IllegalArgumentException.class, () -> cachingService.getWeather(null));
        assertThrows(IllegalArgumentException.class, () -> cachingService.getWeather(""));
        assertThrows(IllegalArgumentException.class, () -> cachingService.getWeather("   "));
    }

    @Test
    void shouldPropagateExceptionsFromWrappedService() throws WeatherSdkException {
        when(mockWeatherService.getWeather("UnknownCity"))
                .thenThrow(new WeatherSdkException("City not found"));

        assertThrows(WeatherSdkException.class, () -> cachingService.getWeather("UnknownCity"));
    }

    private WeatherData createTestWeatherData(String cityName) {
        WeatherData.Weather weather = new WeatherData.Weather("Clear", "clear sky");
        WeatherData.Temperature temp = new WeatherData.Temperature(25.0, 26.0);
        WeatherData.Wind wind = new WeatherData.Wind(5.0);
        WeatherData.Sys sys = new WeatherData.Sys(1675751262L, 1675787560L);

        return new WeatherData(weather, temp, 10000, wind, 1675744800L, sys, 3600, cityName);
    }
}
