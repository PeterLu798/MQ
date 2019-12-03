package com.lbj.mq.cache;

/**
 * @author lubaijiang
 */
public class LruCacheImpl<K, V> extends LruCache<K, V> {

    public LruCacheImpl(int capacity, Storage<K, V> lowSpeedStorage) {
        super(capacity, lowSpeedStorage);
    }

    @Override
    public V get(K key) {
        return null;
    }

    public void testMock(){
        System.out.println("mock");
    }
}
