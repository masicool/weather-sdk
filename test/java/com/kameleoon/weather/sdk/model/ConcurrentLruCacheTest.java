package com.kameleoon.weather.sdk.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для потокобезопасного LRU кеша
 */
class ConcurrentLruCacheTest {
    private ConcurrentLruCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentLruCache<>(3);
    }

    @Test
    void shouldAddAndRetrieveItems() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals(2, cache.size());
    }

    @Test
    void shouldEvictOldestItemWhenCapacityExceeded() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4"); // Должен вытеснить key1

        assertNull(cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals("value3", cache.get("key3"));
        assertEquals("value4", cache.get("key4"));
        assertEquals(3, cache.size());
    }

    @Test
    void shouldUpdateAccessOrderOnGet() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // доступ к key1 перемещает его в конец
        cache.get("key1");

        // добавление нового элемента должно вытеснить key2 (наименее используемый)
        cache.put("key4", "value4");

        assertNull(cache.get("key2"));
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key3"));
        assertNotNull(cache.get("key4"));
    }

    @Test
    void shouldRemoveItems() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        cache.remove("key1");

        assertNull(cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals(1, cache.size());
    }

    @Test
    void shouldClearCache() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        cache.clear();

        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    @Test
    void shouldReturnCorrectMaxSize() {
        assertEquals(3, cache.getMaxSize());
    }

    @Test
    void shouldThrowExceptionForInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () -> new ConcurrentLruCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ConcurrentLruCache<>(-1));
    }
}
