import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;
import com.kameleoon.weather.sdk.exception.AccessDeniedException;
import com.kameleoon.weather.sdk.exception.CityNotFoundException;
import com.kameleoon.weather.sdk.exception.InvalidApiKeyException;
import com.kameleoon.weather.sdk.exception.WeatherSdkException;
import com.kameleoon.weather.sdk.model.SdkMode;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        WeatherSdkManager manager = WeatherSdkManager.getInstance();
        WeatherSdk sdk = manager.getSdk("a28a880098176a505a802319e9e6ee66", SdkMode.ON_DEMAND);

        // тестируем различные сценарии
        String[] testCases = {
                "Moscow",           // корректный город
                "InvalidCity123",   // несуществующий город
                "",                 // пустая строка
                "New York",         // город с пробелом
                "Vladimir",           // корректный город
                "VeryLongCityNameThatProbablyDoesNotExistXYZ" // длинное название
        };

        for (String city : testCases) {
            try {
                System.out.printf("🔍 Запрос погоды для: '%s'%n", city);
                String weather = sdk.getWeather(city);
                System.out.println("✅ Успех: " + weather);

            } catch (CityNotFoundException e) {
                System.out.println("❌ Город не найден: " + e.getMessage());

            } catch (InvalidApiKeyException e) {
                System.out.println("❌ Ошибка API ключа: " + e.getMessage());
                break; // Прерываем тесты если ключ неверный

            } catch (AccessDeniedException e) {
                System.out.println("❌ Превышен лимит запросов: " + e.getMessage());

            } catch (IllegalArgumentException e) {
                System.out.println("❌ Неверные параметры: " + e.getMessage());

            } catch (WeatherSdkException e) {
                System.out.println("❌ Ошибка SDK: " + e.getMessage());

            } catch (Exception e) {
                System.out.println("❌ Неожиданная ошибка: " + e.getMessage());
            }
            System.out.println("---");
        }

        manager.destroySdk("a28a880098176a505a802319e9e6ee66", SdkMode.ON_DEMAND);
    }
}
