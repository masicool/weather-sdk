package com.kameleoon.weather.sdk.model;

import lombok.Getter;

/**
 * Режимы работы SDK для управления поведением получения данных о погоде
 */
@Getter
public enum SdkMode {
    /**
     * On-Demand режим: данные обновляются только при запросе
     * - Использует кеширование (по умолчанию - 10 минут, 10 городов)
     * - Экономит API запросы
     * - Рекомендуется для большинства случаев
     */
    ON_DEMAND("on-demand"),

    /**
     * Polling режим: фоновое обновление данных
     * - Zero-latency ответы из кеша
     * - Фоновое обновление каждые N минут
     * - Для high-performance приложений
     */
    POLLING("polling");

    /**
     * Строковое значение режима для конфигурации
     */
    private final String value;

    SdkMode(String value) {
        this.value = value;
    }

    /**
     * Преобразует строковое значение в соответствующий enum режим
     *
     * @param value строковое представление режима (регистронезависимое)
     * @return соответствующий enum SdkMode
     * @throws IllegalArgumentException если переданное значение не соответствует ни одному из поддерживаемых режимов
     */
    public static SdkMode fromString(String value) {
        if (value == null) {
            return ON_DEMAND; // значение по умолчанию
        }

        String normalized = value.trim().toLowerCase();
        for (SdkMode mode : SdkMode.values()) {
            if (mode.value.equals(normalized)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Unknown SDK mode: " + value +
                ". Supported values: 'on-demand', 'polling'");
    }

    /**
     * Возвращает строковое представление режима для использования в конфигурации
     *
     * @return строковое значение режима
     */
    @Override
    public String toString() {
        return value;
    }
}
