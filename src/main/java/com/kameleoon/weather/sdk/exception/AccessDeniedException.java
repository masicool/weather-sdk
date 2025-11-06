package com.kameleoon.weather.sdk.exception;

/**
 * Исключение при превышении лимита запросов (429 ошибка)
 */
public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String apiMessage) {
        super(429, apiMessage, "Превышен лимит запросов. Попробуйте позже.");
    }
}
