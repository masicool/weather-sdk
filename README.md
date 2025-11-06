
# Weather SDK

[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://java.com)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange.svg)](https://maven.apache.org)

Weather SDK - это Java библиотека для удобной работы с погодными данными с сервиса OpenWeatherMap. SDK предоставляет простой API для получения текущей погоды с поддержкой кэширования, фонового опроса и различных режимов работы.

## Особенности

- 📡 **Получение погодных данных** по названию города
- 💾 **Встроенное кэширование** с LRU стратегией
- 🔄 **Режим фонового опроса** для актуальных данных
- ⚡ **On-demand режим** для запросов по требованию
- 🛡 **Обработка ошибок** с конкретными исключениями
- 🔧 **Гибкая конфигурация** через код, properties-файлы или переменные окружения

## Структура проекта

```
weather-sdk/
├── src/main/java/com/kameleoon/weather/sdk/
│   ├── client/           # Клиент для внешнего API
│   ├── config/           # Конфигурация SDK
│   ├── exception/        # Кастомные исключения
│   ├── model/            # Модели данных
│   └── service/          # Сервисы и основная логика
├── examples/             # Примеры использования
├── src/test/java/        # Тесты
└── resources/            # Ресурсы и настройки
```

## Требования

- Java 17 или выше
- Maven 3.6 или выше
- API ключ от OpenWeatherMap

## Установка

### 1. Сборка из исходников

```bash
git clone https://github.com/masicool/weather-sdk.git
cd weather-sdk
mvn clean install
```

### 2. Добавление зависимости Maven

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
    <groupId>com.kameleoon</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```
Проект использует:

- Java 21 - базовая платформа
- Jackson - работа с JSON
- SLF4J - логирование
- JUnit 5 - тестирование
- Mockito - моки для тестов
- Lombok - уменьшение boilerplate кода

## Быстрый старт

### Базовая настройка

```java
// Создание SDK с API ключом
WeatherSdk sdk = WeatherSdkManager.createSdk("your-api-key");

// Получение погодных данных
WeatherData weather = sdk.getWeather("Moscow");
System.out.println("Температура: " + weather.getTemperature() + "°C");
System.out.println("Влажность: " + weather.getHumidity() + "%");
```

### Расширенная конфигурация

```java
SdkConfig config = SdkConfig.builder()
    .apiKey("your-api-key")
    .cacheSize(100)
    .pollingInterval(10) // минут
    .mode(SdkMode.POLLING)
    .build();

WeatherSdk sdk = WeatherSdkManager.createSdk(config);
```

## Режимы работы

SDK поддерживает два основных режима работы:

### 1. On-Demand режим (`SdkMode.ON_DEMAND`) - по умолчанию
- Запросы к API выполняются только при вызове `getWeather()`
- Подходит для приложений с редкими запросами
- Экономит ресурсы API

### 2. Polling режим (`SdkMode.POLLING`)
- Фоновое обновление данных по расписанию
- Данные всегда актуальны
- Использует кэш для быстрого доступа

## Конфигурация

### Через код
```java
SdkConfig config = SdkConfig.builder("your-api-key")
        .operationMode(OperationMode.POLLING)
        .pollingInterval(5, TimeUnit.MINUTES)    // Интервал обновления
        .maxCacheSize(10)                        // Количество городов в кэше
        .cacheTimeoutMinutes(10)                 // TTL кэша
        .connectTimeoutSeconds(10)               // Таймаут подключения
        .requestTimeoutSeconds(30)               // Таймаут запроса
        .build();
```

### Через properties-файл
Создайте `sdk-config.properties`:
```properties
sdk.api.key=your-api-key
sdk.cache.size=100
sdk.polling.interval=10
sdk.mode=POLLING
```

### Через переменные окружения
```bash
export SDK_API_KEY=your-api-key
export SDK_CACHE_SIZE=100
export SDK_POLLING_INTERVAL=10
export SDK_MODE=POLLING
```

## Примеры использования

В проекте доступны готовые примеры в папке `examples/`:

- `BasicExample` - базовое использование
- `CustomConfigExample` - кастомная конфигурация
- `EnvironmentConfigExample` - конфигурация через переменные окружения
- `ErrorHandlingExample` - обработка ошибок
- `PollingExample` - работа в polling режиме
- `WeatherConsoleApp` - консольное приложение

Запуск примеров:
```bash
cd examples
java -cp target/weather-sdk-1.0.0.jar:target/lib/* BasicExample
```

## Обработка ошибок

SDK выбрасывает конкретные исключения для различных сценариев:

```java
try {
    WeatherData weather = sdk.getWeather("InvalidCity");
} catch (CityNotFoundException e) {
    System.out.println("Город не найден");
} catch (InvalidApiKeyException e) {
    System.out.println("Неверный API ключ");
} catch (AccessDeniedException e) {
    System.out.println("Доступ запрещен");
} catch (WeatherSdkException e) {
    System.out.println("Общая ошибка SDK: " + e.getMessage());
}
```

## Управление жизненным циклом

```java
// Для Polling режима важно закрывать ресурсы
WeatherSdk sdk = new WeatherSdk(config);

// Ваша логика...

// При завершении приложения
sdk.shutdown();
```
## Генерация Javadoc

Для генерации документации выполните:

```bash
mvn javadoc:javadoc
```

Документация будет создана в папке `target/site/apidocs/`

## Тестирование

Запуск тестов:
```bash
mvn test
```

## Логирование

Для настройки логирования используйте `simplelogger.properties`:

```properties
org.slf4j.simpleLogger.log.com.kameleoon.weather.sdk=DEBUG
org.slf4j.simpleLogger.defaultLogLevel=INFO
```

## Модель выходных данных

SDK возвращает объект WeatherData со следующей структурой:
```json
{
  "name": "London",                    // Название города
  "datetime": 1675744800,              // Время данных (Unix timestamp)
  "timezone": 3600,                    // Часовой пояс (секунды)
  "visibility": 10000,                 // Видимость (метры)

  "weather": {                         // Погодные условия
    "main": "Clouds",                  // Основное описание
    "description": "scattered clouds"  // Детальное описание
  },

  "temperature": {                     // Температура
    "temp": 269.6,                     // Текущая температура (Kelvin)
     "feels_like": 267.57               // Ощущаемая температура (Kelvin)
  },

  "wind": {                            // Ветер
    "speed": 1.38                      // Скорость ветра (м/с)
  },

  "sys": {                             // Системная информация
    "sunrise": 1675751262,             // Восход (Unix timestamp)
    "sunset": 1675787560               // Закат (Unix timestamp)
  }
}
```

Пример получения данных в коде:

```java
WeatherData data = sdk.getWeatherByCityName("Paris");

// Основная информация
String cityName = data.getName();
double temperature = data.getTemperature().getTemp();
double feelsLike = data.getTemperature().getFeelsLike();

// Погодные условия
String mainCondition = data.getWeather().getMain();
String description = data.getWeather().getDescription();

// Ветер
double windSpeed = data.getWind().getSpeed();

// Дополнительно
int visibility = data.getVisibility();
long timestamp = data.getDatetime();
int timezone = data.getTimezone();

// Восход/закат
long sunrise = data.getSys().getSunrise();
long sunset = data.getSys().getSunset();
```

**Примечание**: Для работы SDK необходим действительный API ключ от [OpenWeatherMap](https://openweathermap.org/api).
