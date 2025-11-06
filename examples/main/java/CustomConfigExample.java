import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;

/**
 * Пример тонкой настройки конфигурации через Builder
 * - Увеличенный кеш
 * - Кастомные timeout
 * - Специфичные API endpoints (для тестирования)
 */
public class CustomConfigExample {
    public static void main(String[] args) {
        System.out.println("--- Кастомная конфигурация через Builder ---");

        // создаем кастомную конфигурацию
        SdkConfig config = SdkConfig.builder("a28a880098176a505a802319e9e6ee66")
                .mode(SdkMode.ON_DEMAND)
                .cacheTtlMinutes(15)           // кеш на 15 минут
                .maxCacheSize(20)              // до 20 городов в кеше
                .timeoutSeconds(60)            // 60 секунд timeout
                .weatherApiUrl("https://api.openweathermap.org/data/2.5/weather")
                .build();

        WeatherSdk sdk = WeatherSdkManager.getInstance().getSdk(config);

        try {
            // тестируем несколько городов
            String[] europeanCapitals = {
                    "London", "Paris", "Berlin", "Rome", "Madrid",
                    "Amsterdam", "Vienna", "Prague", "Budapest", "Warsaw"
            };

            for (String capital : europeanCapitals) {
                long startTime = System.currentTimeMillis();
                sdk.getWeather(capital);
                long endTime = System.currentTimeMillis();

                System.out.printf("%-12s - %d ms%n", capital, (endTime - startTime));
            }

            System.out.println("\nПовторные запросы будут быстрее (из кеша):");

            // повторные запросы - из кеша
            for (String capital : europeanCapitals) {
                long startTime = System.currentTimeMillis();
                sdk.getWeather(capital);
                long endTime = System.currentTimeMillis();

                System.out.printf("%-12s - %d ms (кеш)%n", capital, (endTime - startTime));
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
