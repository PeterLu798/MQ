package com.lbj.mq.cache;

public abstract class LruCache<K, V> implements Storage<K, V> {
    /**
     *
     */
    protected final int capacity;
    /**
     *
     */
    protected final Storage<K, V> lowSpeedStorage;

    public LruCache(int capacity, Storage<K, V> lowSpeedStorage) {
        this.capacity = capacity;
        this.lowSpeedStorage = lowSpeedStorage;
    }
}
