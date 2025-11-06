package com.kameleoon.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Модель данных для парсинга ответа от OpenWeatherMap Current Weather API 2.5.
 * Содержит полное представление структуры ответа API с аннотациями Jackson
 * для корректного маппинга JSON полей. Игнорирует неизвестные поля для обеспечения
 * обратной совместимости при изменениях API.
 * Внимание: Эта модель используется только для внутреннего парсинга ответов API.
 * Для клиентов SDK используется преобразованная модель WeatherResponse.
 *
 * @see <a href="https://openweathermap.org/current#parameter">OpenWeatherMap Current Weather API Documentation</a>
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiWeatherResponse {

    /**
     * Географические координаты местоположения
     */
    private Coord coord;

    /**
     * Метеорологические данные о погодных условиях
     */
    private List<Weather> weather;

    /**
     * Внутренний параметр API - источник данных
     */
    private String base;

    /**
     * Основные метеорологические данные (температура, давление, влажность)
     */
    private Main main;

    /**
     * Видимость в метрах. Максимальное значение 10,000 метров означает отличную видимость
     */
    private Integer visibility;

    /**
     * Данные о ветре (скорость, направление, порывы)
     */
    private Wind wind;

    /**
     * Облачность в процентах
     */
    private Clouds clouds;

    /**
     * Время расчета данных в Unix timestamp (секунды)
     */
    private Long dt;

    /**
     * Системная информация (страна, время восхода/заката)
     */
    private Sys sys;

    /**
     * Смещение времени от UTC в секундах
     */
    private Integer timezone;

    /**
     * Уникальный идентификатор города в базе OpenWeatherMap
     */
    private Long id;

    /**
     * Название города
     */
    private String name;

    /**
     * Код ответа API. Обычно 200 при успешном запросе
     */
    private Integer cod;

    /**
     * Представляет географические координаты местоположения
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coord {
        /**
         * Долгота в десятичных градусах
         */
        private Double lon;

        /**
         * Широта в десятичных градусах
         */
        private Double lat;
    }

    /**
     * Описывает текущие погодные условия
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        /**
         * Уникальный идентификатор погодных условий
         */
        private Integer id;

        /**
         * Группа погодных параметров (дождь, снег, экстрим и т.д.)
         */
        private String main;

        /**
         * Текстовое описание погодных условий на английском языке
         */
        private String description;

        /**
         * Иконка погодных условий для отображения в UI
         */
        private String icon;
    }

    /**
     * Содержит основные метеорологические показатели
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        /**
         * Температура в указанных единицах измерения
         */
        private Double temp;

        /**
         * Температура по ощущениям человека
         */
        @JsonProperty("feels_like")
        private Double feelsLike;

        /**
         * Минимальная температура в наблюдаемом месте
         */
        @JsonProperty("temp_min")
        private Double tempMin;

        /**
         * Максимальная температура в наблюдаемом месте
         */
        @JsonProperty("temp_max")
        private Double tempMax;

        /**
         * Атмосферное давление в гектопаскалях (hPa)
         */
        private Integer pressure;

        /**
         * Относительная влажность в процентах
         */
        private Integer humidity;

        /**
         * Атмосферное давление на уровне моря в гектопаскалях
         */
        @JsonProperty("sea_level")
        private Integer seaLevel;

        /**
         * Атмосферное давление на уровне земли в гектопаскалях
         */
        @JsonProperty("grnd_level")
        private Integer grndLevel;
    }

    /**
     * Содержит данные о ветровых условиях
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        /**
         * Скорость ветра в метрах в секунду
         */
        private Double speed;

        /**
         * Направление ветра в градусах (метеорологические)
         */
        private Integer deg;

        /**
         * Порыв ветра в метрах в секунду
         */
        private Double gust;
    }

    /**
     * Содержит данные об облачности
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Clouds {
        /**
         * Облачность в процентах. 0% - ясно, 100% - полностью облачно
         */
        private Integer all;
    }

    /**
     * Содержит системную информацию о местоположении
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        /**
         * Внутренний параметр API - тип системы
         */
        private Integer type;

        /**
         * Внутренний параметр API - идентификатор системы
         */
        private Integer id;

        /**
         * Код страны (ISO 3166-1 alpha-2)
         */
        private String country;

        /**
         * Время восхода солнца в Unix timestamp (секунды)
         */
        private Long sunrise;

        /**
         * Время заката солнца в Unix timestamp (секунды)
         */
        private Long sunset;
    }
}
