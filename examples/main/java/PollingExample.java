import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;
import com.kameleoon.weather.sdk.config.SdkConfig;
import com.kameleoon.weather.sdk.model.SdkMode;

/**
 * Пример Polling режима
 */
public class PollingExample {
    public static void main(String[] args) {
        SdkConfig config = SdkConfig.builder("a28a880098176a505a802319e9e6ee66")
                .mode(SdkMode.POLLING)
                .pollingIntervalMinutes(5)
                .build();

        WeatherSdk sdk = null;
        try {
            sdk = WeatherSdkManager.getInstance().getSdk(config);

            // предварительная загрузка данных
            String[] cities = {"Moscow", "Vladimir", "Tambov"};
            for (String city : cities) {
                sdk.getWeather(city);
                System.out.println("Загружено: " + city);
            }

            // демонстрация быстрых запросов
            System.out.println("Тест производительности:");
            for (int i = 0; i < 5; i++) {
                long start = System.currentTimeMillis();
                sdk.getWeather("Moscow");
                long duration = System.currentTimeMillis() - start;
                System.out.printf("Запрос %d: %d ms%n", i + 1, duration);
            }

            // имитация работы приложения
            System.out.println("Ожидание 15 секунд...");
            Thread.sleep(15000);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            // останавливаем polling threads
            if (sdk != null) {
                WeatherSdkManager.getInstance().destroySdk(config);
                System.out.println("Ресурсы polling режима освобождены");
            }
        }
    }
}
