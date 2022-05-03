package com.sap.sse.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.function.Function;

/**
 * A {@link ConcurrentHashMap} can block when calling
 * {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)} because other threads may hold on to
 * the lock of the node required, and the {@link ConcurrentHashMap} guarantees that only one update for the same key is
 * running at any time. If this happens inside a thread managed by a {@link ForkJoinPool}, and the computation to be
 * performed also requires threads from that same thread pool, thread starvation may happen, causing a deadlock.<p>
 * 
 * This can be prevented by using a {@link ManagedBlocker}. See also {@link ForkJoinPool#managedBlock(ManagedBlocker)}.<p>
 * 
 * This class assumes that keys in the map are not removed concurrently to other accesses and hence if a key exists
 * then it is safe to assume that {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} will not have to compute
 * the value and therefore won't block.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ManagedBlockerForComputeIfAbsent<K, V> implements ManagedBlocker {
    private final ConcurrentHashMap<K, V> map;
    private final K key;
    private final Function<K, V> mappingFunction;
    private V result;

    public ManagedBlockerForComputeIfAbsent(ConcurrentHashMap<K, V> map, K key, Function<K, V> mappingFunction) {
        this.map = map;
        this.key = key;
        this.mappingFunction = mappingFunction;
    }

    @Override
    public boolean block() throws InterruptedException {
        if (isReleasable()) {
            result = map.get(key);
        } else {
            result = map.computeIfAbsent(key, mappingFunction);
        }
        return true;
    }

    @Override
    public boolean isReleasable() {
        final boolean releasable = map.containsKey(key);
        if (releasable) {
            result = map.get(key);
        }
        return releasable;
    }

    public V getResult() {
        return result;
    }
}
