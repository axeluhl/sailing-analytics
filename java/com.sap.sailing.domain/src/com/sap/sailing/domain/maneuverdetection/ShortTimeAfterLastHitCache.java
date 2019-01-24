package com.sap.sailing.domain.maneuverdetection;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.util.ThreadPoolUtil;
import com.sap.sse.util.impl.ApproximateTime;

/**
 * Caches entries and ensures that the entries only remain in the cache if they are hit at least one time in the
 * configurable period of time. A separate timer keeps invalidating records. When the cache runs empty the timer is
 * stopped. When new entries appear the timer is started again.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ShortTimeAfterLastHitCache<K, V> {

    private static final Logger logger = Logger.getLogger(ShortTimeAfterLastHitCache.class.getName());
    private final ConcurrentMap<K, ValueWithTimestampSinceLastHit<V>> cache;

    /**
     * Creation and removal / cancellation of the timer is synchronized using {@link #cache}. If this handle is
     * {@code null}, any task that was previously scheduled by this object has been canceled. Otherwise, an uncanceled
     * {@link CacheInvalidator} task is still scheduled to execute at a fixed rate. It can be cancelled using this
     * future.
     */
    private ScheduledFuture<?> invalidatorHandle;

    private final long preserveHowManyMilliseconds;
    private final UncachedValueRetrieverCallback<K, V> uncachedValueRetrieverCallback;
    private final CachedValueCleaningCallback<K, V> cachedValueCleaningCallback;

    private long hits;
    private long misses;

    private class CacheInvalidator implements Runnable {
        @Override
        public void run() {
            long oldestToKeep = System.currentTimeMillis() - preserveHowManyMilliseconds;
            for (Iterator<Entry<K, ValueWithTimestampSinceLastHit<V>>> iterator = cache.entrySet().iterator(); iterator
                    .hasNext();) {
                Entry<K, ValueWithTimestampSinceLastHit<V>> entry = iterator.next();
                if (entry.getValue().getTimestampSinceLastHit() < oldestToKeep) {
                    iterator.remove();
                    if (cachedValueCleaningCallback != null) {
                        cachedValueCleaningCallback.cleanValue(entry.getKey(), entry.getValue().getValue());
                    }
                }
            }
            synchronized (cache) {
                if (cache.isEmpty()) {
                    invalidatorHandle.cancel(/* mayInterruptIfRunning */ false);
                    invalidatorHandle = null;
                }
            }
        }
    }

    public void clearCache() {
        if (cachedValueCleaningCallback != null) {
            for (Iterator<Entry<K, ValueWithTimestampSinceLastHit<V>>> iterator = cache.entrySet().iterator(); iterator
                    .hasNext();) {
                Entry<K, ValueWithTimestampSinceLastHit<V>> entry = iterator.next();
                iterator.remove();
                if (cachedValueCleaningCallback != null) {
                    cachedValueCleaningCallback.cleanValue(entry.getKey(), entry.getValue().getValue());
                }
            }
        } else {
            cache.clear();
        }
        synchronized (cache) {
            if (cache.isEmpty()) {
                invalidatorHandle.cancel(/* mayInterruptIfRunning */ false);
                invalidatorHandle = null;
            }
        }
    }

    /**
     * Constructs a new empty cache.
     * 
     * @param preserveHowManyMilliseconds
     *            The period by which cache entries get invalidated, if they are not hit at least one time within the
     *            provided period
     * @param uncachedValueRetrieverCallback
     *            Is used to retrieve values which are not contained in this cache
     */
    public ShortTimeAfterLastHitCache(long preserveHowManyMilliseconds,
            UncachedValueRetrieverCallback<K, V> uncachedValueRetrieverCallback) {
        this(preserveHowManyMilliseconds, uncachedValueRetrieverCallback, null);
    }

    /**
     * Constructs a new empty cache.
     * 
     * @param preserveHowManyMilliseconds
     *            The period by which cache entries get invalidated, if they are not hit at least one time within the
     *            provided period
     * @param uncachedValueRetrieverCallback
     *            Is used to retrieve values which are not contained in this cache
     * @param cachedValueCleaningCallback
     *            Callback which gets called, when a key-value pair gets removed from cache
     */
    public ShortTimeAfterLastHitCache(long preserveHowManyMilliseconds,
            UncachedValueRetrieverCallback<K, V> uncachedValueRetrieverCallback,
            CachedValueCleaningCallback<K, V> cachedValueCleaningCallback) {
        this.preserveHowManyMilliseconds = preserveHowManyMilliseconds;
        this.uncachedValueRetrieverCallback = uncachedValueRetrieverCallback;
        this.cache = new ConcurrentHashMap<>();
        this.cachedValueCleaningCallback = cachedValueCleaningCallback;
    }

    public void addToCache(K key, V value) {
        final long timestamp = calculateCurrentTimestamp();
        synchronized (cache) {
            cache.put(key, new ValueWithTimestampSinceLastHit<>(value, timestamp));
            ensureTimerIsRunning();
        }
    }

    private long calculateCurrentTimestamp() {
        final long timestamp;
        if (preserveHowManyMilliseconds > 1000) {
            timestamp = ApproximateTime.approximateNow().asMillis();
        } else {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public V getValue(K key) {
        V value = getCachedValue(key);
        if (value == null) {
            value = uncachedValueRetrieverCallback.getUncachedValue(key);
            if (value != null) {
                addToCache(key, value);
            }
        }
        return value;
    }

    public V getCachedValue(K key) {
        ValueWithTimestampSinceLastHit<V> valueWrapper = cache.get(key);
        V value;
        if (valueWrapper == null) {
            misses++;
            value = null;
        } else {
            hits++;
            valueWrapper.setTimestampSinceLastHit(calculateCurrentTimestamp());
            value = valueWrapper.getValue();
        }
        if ((hits + misses) % 100000l == 0 && logger.isLoggable(Level.FINE)) {
            logger.fine("hits: " + hits + ", misses: " + misses);
        }
        return value;
    }

    /**
     * Must be called while owning the {@link #cache} monitor (synchronized)
     */
    private void ensureTimerIsRunning() {
        if (preserveHowManyMilliseconds != Long.MAX_VALUE && invalidatorHandle == null) {
            invalidatorHandle = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor()
                    .scheduleAtFixedRate(new CacheInvalidator(), /* delay */ preserveHowManyMilliseconds,
                            preserveHowManyMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    public interface UncachedValueRetrieverCallback<K, V> {
        V getUncachedValue(K key);
    }

    public interface CachedValueCleaningCallback<K, V> {
        void cleanValue(K key, V value);
    }

    private static class ValueWithTimestampSinceLastHit<V> {
        private final V value;
        private volatile long timestampSinceLastHit;

        public ValueWithTimestampSinceLastHit(V value, long timestampSinceLastHit) {
            this.value = value;
            this.timestampSinceLastHit = timestampSinceLastHit;
        }

        public long getTimestampSinceLastHit() {
            return timestampSinceLastHit;
        }

        public void setTimestampSinceLastHit(long timePointSinceAlive) {
            this.timestampSinceLastHit = timePointSinceAlive;
        }

        public V getValue() {
            return value;
        }
    }
}
