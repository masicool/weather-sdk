package com.kameleoon.weather.sdk.exception;

/**
 * Исключение при неверном API ключе (401 ошибка)
 */
public class InvalidApiKeyException extends ApiException {
    public InvalidApiKeyException(String apiMessage) {
        super(401, apiMessage, "Неверный API ключ. Проверьте правильность ключа.");
    }
}