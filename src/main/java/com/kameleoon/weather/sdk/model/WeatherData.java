package com.kameleoon.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Модель данных о погоде для ответа клиентам SDK
 * <p>
 * Представляет данные в формате, соответствующем техническому заданию.
 * Используется для преобразования сырых данных API в структурированный ответ.
 */
@Setter
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    /**
     * Информация о погодных условиях
     */
    private Weather weather;

    /**
     * Температурные показатели
     */
    private Temperature temperature;

    /**
     * Видимость в метрах
     */
    private int visibility;

    /**
     * Данные о ветре
     */
    private Wind wind;

    /**
     * Время расчета данных в формате Unix timestamp
     */
    private long datetime;

    /**
     * Системная информация (время восхода/заката)
     */
    private Sys sys;

    /**
     * Смещение времени от UTC в секундах
     */
    private int timezone;

    /**
     * Название города
     */
    private String name;

    /**
     * Описание погодных условий
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class Weather {
        /**
         * Основная характеристика погоды (например, "Clouds", "Rain")
         */
        private String main;

        /**
         * Детальное описание погодных условий
         */
        private String description;
    }

    /**
     * Температурные показатели
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class Temperature {
        /**
         * Фактическая температура в градусах Цельсия
         */
        private double temp;

        /**
         * Температура по ощущениям в градусах Цельсия
         */
        @JsonProperty("feels_like")
        private double feelsLike;
    }

    /**
     * Данные о ветровых условиях
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class Wind {
        /**
         * Скорость ветра в метрах в секунду
         */
        private double speed;
    }

    /**
     * Системная информация о местоположении
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class Sys {
        /**
         * Время восхода солнца в формате Unix timestamp
         */
        private long sunrise;

        /**
         * Время заката солнца в формате Unix timestamp
         */
        private long sunset;
    }
}
