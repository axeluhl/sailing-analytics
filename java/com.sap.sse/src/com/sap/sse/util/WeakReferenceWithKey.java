package com.sap.sse.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * A weak reference that, when constructed, enters itself into a given map from where it can be removed
 * again using {@link #cleanUp}. The {@link #cleanUp()} method should be called after this weak reference
 * has been enqueued into the {@link ReferenceQueue} passed to the constructor.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <R>
 *            the referent's type
 * @param <K>
 *            the key's type
 */
public class WeakReferenceWithKey<K, R> extends WeakReference<R> {
    private final K key;
    private final Map<K, WeakReferenceWithKey<K, R>> map;
    private volatile boolean wasCleanedUp;
    
    public WeakReferenceWithKey(K key, R referent, Map<K, WeakReferenceWithKey<K, R>> map, ReferenceQueue<? super R> q) {
        super(referent, q);
        this.wasCleanedUp = false;
        this.key = key;
        this.map = map;
        this.map.put(key, this);
    }

    /**
     * Assuming that this weak reference was entered into {@link #map} using key {@link 
     */
    public void cleanUp() {
        if (!wasCleanedUp) {
            this.map.remove(this.key);
            wasCleanedUp = true;
        }
    }
}
