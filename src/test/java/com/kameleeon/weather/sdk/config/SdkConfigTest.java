package com.kameleeon.weather.sdk.config;

import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты для конфигурации SDK
 */
class SdkConfigTest {

    @Test
    void shouldCreateConfigWithRequiredParameters() {
        SdkConfig config = new SdkConfig("test-api-key");

        assertEquals("test-api-key", config.getApiKey());
        assertEquals(SdkMode.ON_DEMAND, config.getMode());
        assertEquals(10, config.getCacheTtlMinutes());
        assertEquals(10, config.getMaxCacheSize());
    }

    @Test
    void shouldCreateConfigWithBuilder() {
        SdkConfig config = SdkConfig.builder("test-api-key")
                .mode(SdkMode.POLLING)
                .cacheTtlMinutes(15)
                .maxCacheSize(20)
                .timeoutSeconds(60)
                .build();

        assertEquals("test-api-key", config.getApiKey());
        assertEquals(SdkMode.POLLING, config.getMode());
        assertEquals(15, config.getCacheTtlMinutes());
        assertEquals(20, config.getMaxCacheSize());
        assertEquals(60, config.getTimeoutSeconds());
    }

    @Test
    void shouldThrowExceptionForNullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new SdkConfig(null));
        assertThrows(IllegalArgumentException.class, () -> new SdkConfig(""));
        assertThrows(IllegalArgumentException.class, () -> new SdkConfig("   "));
    }

    @Test
    void shouldCreateConfigWithModeConstructor() {
        SdkConfig config = new SdkConfig("test-key", SdkMode.POLLING);

        assertEquals("test-key", config.getApiKey());
        assertEquals(SdkMode.POLLING, config.getMode());
    }
}
