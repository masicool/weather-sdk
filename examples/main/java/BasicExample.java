import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;
import com.kameleoon.weather.sdk.model.SdkMode;

/**
 * Простой пример использования SDK
 * Автоматическая загрузка конфигурации по умолчанию
 * (On-Demand режим, кеширование 10 городов на 10 минут)
 */
public class BasicExample {
    public static void main(String[] args) {
        System.out.println("--- Базовое использование SDK ---");

        WeatherSdk sdk = null;
        try {
            // указываем только API ключ
            sdk = WeatherSdkManager.getInstance().getSdk("a28a880098176a505a802319e9e6ee66");

            // первый запрос - идет в API
            String weather = sdk.getWeather("Moscow");
            System.out.println("Погода в Москве:");
            System.out.println(weather);

            // второй запрос - может быть из кеша (если прошло <10 минут)
            weather = sdk.getWeather("Vladimir");
            System.out.println("Погода во Владимире:");
            System.out.println(weather);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            // освобождаем ресурсы
            if (sdk != null) {
                WeatherSdkManager.getInstance().destroySdk("a28a880098176a505a802319e9e6ee66", SdkMode.ON_DEMAND);
            }
        }
    }
}
