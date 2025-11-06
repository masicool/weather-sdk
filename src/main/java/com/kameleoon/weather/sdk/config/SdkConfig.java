package com.kameleoon.weather.sdk.config;

import com.kameleoon.weather.sdk.model.SdkMode;
import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;

@Getter
public class SdkConfig {
    private final String apiKey;
    private final SdkMode mode;
    private final long cacheTtlMinutes;
    private final int maxCacheSize;
    private final long pollingIntervalMinutes;
    private final String weatherApiUrl;
    private final int timeoutSeconds;

    private SdkConfig(Builder builder) {
        this.apiKey = builder.apiKey;
        this.mode = builder.mode;
        this.cacheTtlMinutes = builder.cacheTtlMinutes;
        this.maxCacheSize = builder.maxCacheSize;
        this.pollingIntervalMinutes = 10L;
        this.weatherApiUrl = builder.weatherApiUrl;
        this.timeoutSeconds = builder.timeoutSeconds;
    }

    public SdkConfig(String apiKey) {
        this(new Builder(apiKey));
    }

    public SdkConfig(String apiKey, SdkMode mode) {
        this(builder(apiKey).mode(mode));
    }

    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    public static class Builder {
        private final String apiKey;
        private SdkMode mode;
        private long cacheTtlMinutes;
        private int maxCacheSize;
        private long pollingIntervalMinutes = 10L;
        private String weatherApiUrl;
        private int timeoutSeconds;

        public Builder(String apiKey) {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key cannot be null or empty");
            }
            this.apiKey = apiKey.trim();
            loadDefaults();
            loadFromEnvironment();
        }

        private void loadDefaults() {
            Properties defaults = loadProperties("/sdk-defaults.properties");
            this.mode = SdkMode.fromString(getProperty(defaults, "sdk.default.mode", "on-demand"));
            this.cacheTtlMinutes = getLongProperty(defaults, "sdk.default.cache.ttl.minutes", 10L);
            this.maxCacheSize = getIntProperty(defaults, "sdk.default.cache.max.size", 10);
            this.pollingIntervalMinutes = getLongProperty(defaults,
                    "sdk.default.polling.interval.minutes", 10L);
            this.weatherApiUrl = getProperty(defaults, "sdk.default.weather.api.url",
                    "https://api.openweathermap.org/data/2.5/weather");
            this.timeoutSeconds = getIntProperty(defaults, "sdk.default.timeout.seconds", 30);
        }

        private void loadFromEnvironment() {
            this.mode = SdkMode.fromString(getEnv("WEATHER_SDK_MODE", this.mode.getValue()));
            this.cacheTtlMinutes = getLongEnv("WEATHER_SDK_CACHE_TTL", this.cacheTtlMinutes);
            this.maxCacheSize = getIntEnv("WEATHER_SDK_CACHE_SIZE", this.maxCacheSize);
            this.pollingIntervalMinutes = getLongEnv("WEATHER_SDK_POLLING_INTERVAL", this.pollingIntervalMinutes);
            this.weatherApiUrl = getEnv("WEATHER_API_URL", this.weatherApiUrl);
            this.timeoutSeconds = getIntEnv("API_TIMEOUT_SECONDS", this.timeoutSeconds);
        }

        private Properties loadProperties(String filename) {
            Properties props = new Properties();
            try (InputStream input = getClass().getResourceAsStream(filename)) {
                if (input != null) props.load(input);
            } catch (Exception e) {
                // используем хардкодные значения по умолчанию
            }
            return props;
        }

        private String getProperty(Properties props, String key, String defaultValue) {
            return props.getProperty(key, defaultValue);
        }

        private int getIntProperty(Properties props, String key, int defaultValue) {
            try {
                return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private long getLongProperty(Properties props, String key, long defaultValue) {
            try {
                return Long.parseLong(props.getProperty(key, String.valueOf(defaultValue)));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private String getEnv(String key, String defaultValue) {
            return System.getenv().getOrDefault(key, defaultValue);
        }

        private int getIntEnv(String key, int defaultValue) {
            String value = System.getenv(key);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) { /* keep default */ }
            }
            return defaultValue;
        }

        private long getLongEnv(String key, long defaultValue) {
            String value = System.getenv(key);
            if (value != null) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) { /* keep default */ }
            }
            return defaultValue;
        }

        public Builder mode(SdkMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder cacheTtlMinutes(long minutes) {
            this.cacheTtlMinutes = minutes;
            return this;
        }

        public Builder maxCacheSize(int size) {
            this.maxCacheSize = size;
            return this;
        }

        public Builder pollingIntervalMinutes(long minutes) {
            this.pollingIntervalMinutes = minutes;
            return this;
        }

        public Builder weatherApiUrl(String url) {
            this.weatherApiUrl = url;
            return this;
        }

        public Builder timeoutSeconds(int seconds) {
            this.timeoutSeconds = seconds;
            return this;
        }

        public SdkConfig build() {
            return new SdkConfig(this);
        }
    }
}

