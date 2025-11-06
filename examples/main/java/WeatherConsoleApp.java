import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.weather.sdk.WeatherSdk;
import com.kameleoon.weather.sdk.WeatherSdkManager;
import com.kameleoon.weather.sdk.model.SdkMode;

import java.util.Scanner;


public class WeatherConsoleApp {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static WeatherSdk sdk;
    private static Scanner scanner;

    public static void main(String[] args) {
        initializeSdk();
        scanner = new Scanner(System.in);

        System.out.println("🌤️  Добро пожаловать в Weather SDK Demo!");

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> getWeatherForCity();
                case "2" -> compareCities();
                case "3" -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор");
            }
        }
    }

    private static void initializeSdk() {
        WeatherSdkManager manager = WeatherSdkManager.getInstance();
        sdk = manager.getSdk("a28a880098176a505a802319e9e6ee66", SdkMode.ON_DEMAND);

        // добавляем shutdown hook для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                manager.destroySdk("a28a880098176a505a802319e9e6ee66", SdkMode.ON_DEMAND)));
    }

    private static void printMenu() {
        System.out.println("\n--- ГЛАВНОЕ МЕНЮ ---");
        System.out.println("1. Узнать погоду в городе");
        System.out.println("2. Сравнить погоду в двух городах");
        System.out.println("3. Выход");
        System.out.print("Выберите опцию: ");
    }

    private static void getWeatherForCity() {
        System.out.print("Введите название города: ");
        String city = scanner.nextLine();

        try {
            String weatherJson = sdk.getWeather(city);
            System.out.println(weatherJson);
            JsonNode data = mapper.readTree(weatherJson);

            System.out.println("\n--- ПОГОДА В " + data.get("name").asText().toUpperCase() + " ---");
            System.out.println("🌡️  Температура: " + data.get("temperature").get("temp").asDouble() + "°C");
            System.out.println("🤔 Ощущается как: " + data.get("temperature").get("feels_like").asDouble() + "°C");
            System.out.println("☁️ Описание: " + data.get("weather").get("description").asText());
            System.out.println("💨 Скорость ветра: " + data.get("wind").get("speed").asDouble() + " м/с");
            System.out.println("👁️ Видимость: " + data.get("visibility").asInt() + " метров");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void compareCities() {
        System.out.print("Введите первый город: ");
        String city1 = scanner.nextLine();
        System.out.print("Введите второй город: ");
        String city2 = scanner.nextLine();

        try {
            String weather1 = sdk.getWeather(city1);
            String weather2 = sdk.getWeather(city2);

            JsonNode data1 = mapper.readTree(weather1);
            JsonNode data2 = mapper.readTree(weather2);

            System.out.println("\n--- СРАВНЕНИЕ ПОГОДЫ ---");
            System.out.printf("%-15s | %-15s%n", city1, city2);
            System.out.println("----------------|-----------------");
            System.out.printf("%-15.1f | %-15.1f%n",
                    data1.get("main").get("temp").asDouble(),
                    data2.get("main").get("temp").asDouble());

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
}
