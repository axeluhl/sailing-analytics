package com.sap.sse.util;

import java.lang.ref.ReferenceQueue;
import java.util.Map;

public class WeakValueCache<K, V> {
    private final Map<K, WeakReferenceWithKey<K, V>> map;
    private final ReferenceQueue<V> queue;
    
    public WeakValueCache(final Map<K, WeakReferenceWithKey<K, V>> map) {
        this.map = map;
        queue = new ReferenceQueue<>();
    }
    
    public V get(K k) {
        purge();
        WeakReferenceWithKey<K, V> result = map.get(k);
        return result == null ? null : result.get();
    }
    
    public V put(K k, V v) {
        purge();
        final V result;
        final WeakReferenceWithKey<K, V> old = map.get(k);
        if (old != null) {
            old.cleanUp();
            result = old.get();
        } else {
            result = null;
        }
        map.put(k, new WeakReferenceWithKey<>(k, v, map, queue));
        return result;
    }
    
    public V remove(K k) {
        purge();
        final V result;
        final WeakReferenceWithKey<K, V> ref = map.get(k);
        if (ref != null) {
            result = ref.get();
            ref.cleanUp();
        } else {
            result = null;
        }
        return result;
    }

    private void purge() {
        WeakReferenceWithKey<?, ?> r;
        while ((r=(WeakReferenceWithKey<?, ?>) queue.poll()) != null) {
            r.cleanUp();
        }
    }
}
