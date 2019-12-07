package com.lbj.mq.cache;

public interface Storage<K, V> {
    /**
     *
     *
     * @param key
     * @return
     */
    V get(K key);
}
