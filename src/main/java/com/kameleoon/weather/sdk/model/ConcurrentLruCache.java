package com.kameleoon.weather.sdk.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Потокобезопасный LRU кеш на основе ConcurrentHashMap.
 * Гарантирует корректную работу в многопоточной среде.
 * Автоматически удаляет наименее используемые элементы при превышении лимита.
 */
public class ConcurrentLruCache<K, V> {
    private final int maxSize;
    private final Map<K, V> cache;
    private final ConcurrentLinkedQueue<K> accessQueue;
    private final Lock lock;

    /**
     * Создает LRU кеш с указанным максимальным размером
     *
     * @param maxSize максимальное количество элементов в кеше
     * @throws IllegalArgumentException если maxSize меньше или равен 0
     */
    public ConcurrentLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }

        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();
        this.accessQueue = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantLock();
    }

    /**
     * Добавляет элемент в кеш
     * Если элемент уже существует, обновляет его позицию в очереди доступа.
     * Автоматически удаляет наименее используемый элемент при превышении лимита.
     *
     * @param key   ключ элемента
     * @param value значение элемента
     */
    public void put(K key, V value) {
        lock.lock();
        try {
            // если ключ уже существует, обновляем очередь
            if (cache.containsKey(key)) {
                accessQueue.remove(key);
            }

            // добавляем новый элемент
            cache.put(key, value);
            accessQueue.add(key);

            // проверяем размер и удаляем самый старый если нужно
            while (cache.size() > maxSize) {
                K oldestKey = accessQueue.poll();
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Получает элемент из кеша
     * При получении элемента обновляет его позицию в очереди доступа,
     * помечая его как недавно использованный.
     *
     * @param key ключ элемента
     * @return значение элемента или null если не найден
     */
    public V get(K key) {
        lock.lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                // Обновляем порядок доступа
                accessQueue.remove(key);
                accessQueue.add(key);
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Проверяет наличие элемента в кеше
     *
     * @param key ключ элемента
     * @return true если элемент присутствует в кеше
     */
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    /**
     * Удаляет элемент из кеша
     *
     * @param key ключ элемента
     */
    public void remove(K key) {
        lock.lock();
        try {
            cache.remove(key);
            accessQueue.remove(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Очищает кеш
     */
    public void clear() {
        lock.lock();
        try {
            cache.clear();
            accessQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Возвращает текущий размер кеша
     *
     * @return количество элементов в кеше
     */
    public int size() {
        return cache.size();
    }

    /**
     * Возвращает максимальный размер кеша
     *
     * @return максимальное количество элементов
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Возвращает набор ключей для итерации по кешу
     * <p>
     * Возвращает копию текущего набора ключей для безопасной итерации.
     * Полезно для операций, требующих обхода всех элементов кеша.
     *
     * @return набор ключей кеша
     */
    public Set<K> getKeys() {
        lock.lock();
        try {
            // Возвращаем копию для безопасной итерации
            return Set.copyOf(cache.keySet());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Проверяет пуст ли кеш
     *
     * @return true если кеш не содержит элементов
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }
}
