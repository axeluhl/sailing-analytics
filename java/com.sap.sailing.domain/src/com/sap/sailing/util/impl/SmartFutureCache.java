package com.sap.sailing.util.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import com.sap.sailing.util.impl.SmartFutureCache.UpdateInterval;

/**
 * A cache for which a background update can be triggered. Readers can decide whether they want to wait for any ongoing
 * background update or read the latest cached value for a key. An update trigger can provide an optional parameter for
 * the update which may, e.g., control the interval of the cached value to update. When an update is triggered and
 * another update is already running, the update is queued. If there already is an update queued for the same key,
 * the optional update parameters are "joined" (for example, the two update intervals are joined to form one interval
 * which incorporates both original update intervals).
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class SmartFutureCache<K, V, U extends UpdateInterval> {
    private static final Logger logger = Logger.getLogger(SmartFutureCache.class.getName());
    
    /**
     * Holds the tasks that have been added to an {@link Executor} already for execution and that, as long as a client
     * holds the object monitor / lock on this map, aren't cancelled. Note, however, that once the lock is released,
     * the Futures may be cancelled in case a cache replacement has taken place. Clients can prevent this by calling
     * {@link FutureTaskWithCancelBlocking#dontCancel()} on the future while holding the lock on
     * {@link #ongoingManeuverCacheRecalculations}. This will let {@link Future#cancel(boolean)} return <code>false</code>
     * should it be called on that Future.
     */
    private final Map<K, FutureTaskWithCancelBlocking<V, U>> ongoingRecalculations;
    
    private final Map<K, V> cache;
    
    private final Executor recalculator;

    private final CacheUpdateComputer<K, V, U> cacheUpdateComputer;
    
    /**
     * An immutable "interval" description for a cache update
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public static interface UpdateInterval {
        /**
         * Produces a new immutable update interval that "contains" both, this and <code>otherUpdateInterval</code> according
         * to the semantics of the specific implementation.
         */
        <U extends UpdateInterval> U join(U otherUpdateInterval);
    }
    
    /**
     * For a key and an optional update interval can compute a new value to be stored in the cache.
     * @author Axel Uhl (D043530)
     *
     * @param <K> the cache's key type
     * @param <V> the cache's value type
     * @param <U> the update interval type
     */
    public static interface CacheUpdateComputer<K, V, U extends UpdateInterval> {
        V computeCacheValue(K key, U updateInterval) throws Exception;
    }
    
    /**
     * Once a client has fetched such a Future from {@link TrackedRaceImpl##ongoingManeuverCacheRecalculations} while
     * holding the object monitor of {@link TrackedRaceImpl##ongoingManeuverCacheRecalculations}, the client knows that
     * the Future hasn't been cancelled yet. To avoid that the Future is cancelled after the client has fetched it from
     * {@link TrackedRaceImpl##ongoingManeuverCacheRecalculations}, the client can call {@link #dontCancel()} on this
     * future. After that, calls to {@link #cancel(boolean)} will return <code>false</code> immediately and the Future
     * will be executed as originally scheduled.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    private static class FutureTaskWithCancelBlocking<V, U extends UpdateInterval> extends FutureTask<V> {
        private boolean dontCancel;
        
        private final U updateInterval;
        
        public FutureTaskWithCancelBlocking(Callable<V> callable, U updateInterval) {
            super(callable);
            this.updateInterval = updateInterval;
        }

        public synchronized void dontCancel() {
            dontCancel = true;
        }
        
        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            if (!dontCancel) {
                return super.cancel(mayInterruptIfRunning);
            } else {
                return false;
            }
        }

        public U getUpdateInterval() {
            return updateInterval;
        }
    }
    
    public SmartFutureCache(CacheUpdateComputer<K, V, U> cacheUpdateComputer) {
        this.ongoingRecalculations = new HashMap<K, FutureTaskWithCancelBlocking<V, U>>();
        this.cache = new HashMap<K, V>();
        this.recalculator = Executors.newSingleThreadExecutor();
        this.cacheUpdateComputer = cacheUpdateComputer;
    }
    
    public void triggerUpdate(final K key, U updateInterval) {
        // establish and maintain the following invariant: after lock on ongoingManeuverCacheRecalculations is released,
        // no Future contained in it is in cancelled state
        final FutureTaskWithCancelBlocking<V, U> future;
        synchronized (ongoingRecalculations) {
            FutureTaskWithCancelBlocking<V, U> oldFuture = ongoingRecalculations.get(key);
            final U joinedUpdateInterval;
            if (oldFuture == null) {
                joinedUpdateInterval = updateInterval;
            } else {
                oldFuture.cancel(/* mayInterruptIfRunning */false);
                if (updateInterval == null) {
                    joinedUpdateInterval = oldFuture.getUpdateInterval();
                } else {
                    joinedUpdateInterval = updateInterval.join(oldFuture.getUpdateInterval());
                }
            }
            future = new FutureTaskWithCancelBlocking<V, U>(
                    new Callable<V>() {
                        @Override
                        public V call() {
                            try {
                                V result = cacheUpdateComputer.computeCacheValue(key, joinedUpdateInterval);
                                synchronized (cache) {
                                    if (result == null) {
                                        cache.remove(key);
                                    } else {
                                        cache.put(key, result);
                                    }
                                }
                                return result;
                            } catch (Throwable e) {
                                // cache won't be updated
                                logger.throwing(SmartFutureCache.class.getName(), "triggerUpdate", e);
                                throw new RuntimeException(e);
                            }
                        }
                    }, joinedUpdateInterval);
            ongoingRecalculations.put(key, future);
        }
        recalculator.execute(future);
    }
    
    public V get(K key, boolean waitForLatest) {
        V value;
        if (waitForLatest) {
            FutureTaskWithCancelBlocking<V, U> future;
            synchronized (ongoingRecalculations) {
                // as long as we hold the lock on ongoingManeuverCacheRecalculations, the Futures contained in it are not cancelled
                future = ongoingRecalculations.get(key);
                if (future != null) {
                    future.dontCancel();
                }
            }
            if (future != null) {
                try {
                    value = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // no calculation currently going on; we probably don't know anything about this key
                value = null;
            }
        } else {
            synchronized (cache) {
                value = cache.get(key);
            }
        }
        return value;
    }
}
