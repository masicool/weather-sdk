import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;

/**
 * Пример использования environment variables для конфигурации
 * Перед запуском установите переменные окружения:
 * export OPENWEATHER_API_KEY="your-actual-api-key"
 * export WEATHER_SDK_MODE="on-demand"
 * export WEATHER_SDK_CACHE_TTL="10"
 * export WEATHER_SDK_CACHE_SIZE="10"
 * export API_TIMEOUT_SECONDS="60"
 */
public class EnvironmentConfigExample {
    public static void main(String[] args) {
        System.out.println("--- Конфигурация через Environment Variables ---");

        try {
            // настройки загружаются из environment variables
            WeatherSdk sdk = WeatherSdkManager.getInstance().getSdk();

            System.out.println("Конфигурация загружена из environment variables");

            String[] cities = {"Moscow", "Tambov", "Vladimir"};
            for (String city : cities) {
                String weather = sdk.getWeather(city);
                System.out.println("Погода в " + city + ":");
                System.out.println(weather);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.err.println("Убедитесь, что установлена переменная OPENWEATHER_API_KEY");
        }
    }
}
