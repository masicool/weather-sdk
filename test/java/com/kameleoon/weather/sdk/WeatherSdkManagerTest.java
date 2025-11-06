package com.kameleoon.weather.sdk;

import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для менеджера SDK
 */
class WeatherSdkManagerTest {
    private WeatherSdkManager manager;

    @BeforeEach
    void setUp() {
        manager = WeatherSdkManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        // очищаем инстансы после каждого теста
        manager.destroySdk("key1", SdkMode.ON_DEMAND);
        manager.destroySdk("key2", SdkMode.POLLING);
    }

    @Test
    void shouldReturnSameInstanceForSameConfig() {
        SdkConfig config1 = new SdkConfig("test-key", SdkMode.ON_DEMAND);
        SdkConfig config2 = new SdkConfig("test-key", SdkMode.ON_DEMAND);

        WeatherSdk sdk1 = manager.getSdk(config1);
        WeatherSdk sdk2 = manager.getSdk(config2);

        assertSame(sdk1, sdk2);
    }

    @Test
    void shouldReturnDifferentInstancesForDifferentApiKeys() {
        WeatherSdk sdk1 = manager.getSdk("key1", SdkMode.ON_DEMAND);
        WeatherSdk sdk2 = manager.getSdk("key2", SdkMode.ON_DEMAND);

        assertNotSame(sdk1, sdk2);
    }

    @Test
    void shouldReturnDifferentInstancesForDifferentModes() {
        WeatherSdk sdk1 = manager.getSdk("same-key", SdkMode.ON_DEMAND);
        WeatherSdk sdk2 = manager.getSdk("same-key", SdkMode.POLLING);

        assertNotSame(sdk1, sdk2);
    }

    @Test
    void shouldDestroySdkInstance() {
        WeatherSdk sdk = manager.getSdk("test-key", SdkMode.ON_DEMAND);

        manager.destroySdk("test-key", SdkMode.ON_DEMAND);

        // после уничтожения должен быть создан новый инстанс
        WeatherSdk newSdk = manager.getSdk("test-key", SdkMode.ON_DEMAND);
        assertNotSame(sdk, newSdk);
    }

    @Test
    void shouldThrowExceptionForInvalidApiKey() {
        assertThrows(IllegalArgumentException.class, () -> manager.getSdk((SdkConfig) null));
        assertThrows(IllegalArgumentException.class, () -> manager.getSdk(""));
    }
}
