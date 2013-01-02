package com.sap.sailing.util.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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
 * another update is already running, the update is queued. If there already is an update queued for the same key, the
 * optional update parameters are "joined" (for example, the two update intervals are joined to form one interval which
 * incorporates both original update intervals).
 * <p>
 * 
 * A {@link CacheUpdater} needs to be passed to the constructor which carries out the actual calculation whose values
 * are to be cached. The {@link CacheUpdater} interface assumes that a cache update may be computed in two steps: first,
 * a value is computed for a key and an update interval which may be computationally expensive. Then, in a second step,
 * the new value is combined with the previous cache value for the same key and update interval. The default
 * implementation of {@link CacheUpdater#provideNewCacheValue(Object, Object, Object, UpdateInterval)} simply returns
 * the <code>computedCacheUpdate</code> parameter which is the result computed by
 * {@link CacheUpdater#computeCacheUpdate(Object, UpdateInterval)} before.
 * 
 * @param <K>
 *            the key type for which values of type <code>V</code> are cached
 * @param <V>
 *            the value type of which instances are cached for particular keys of type <code>K</code>
 * @param <U>
 *            a parameter type for the cache update method for a single key, such that the parameters of multiple queued
 *            requests for the same key can be joined into one for a faster update
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class SmartFutureCache<K, V, U extends UpdateInterval<U>> {
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

    private final CacheUpdater<K, V, U> cacheUpdateComputer;
    
    private final Map<K, NamedReentrantReadWriteLock> locksForKeys;
    
    private final String nameForLocks;
    
    /**
     * An immutable "interval" description for a cache update
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public static interface UpdateInterval<U extends UpdateInterval<U>> {
        /**
         * Produces a new immutable update interval that "contains" both, this and <code>otherUpdateInterval</code> according
         * to the semantics of the specific implementation.
         */
        U join(U otherUpdateInterval);
    }
    
    public static class EmptyUpdateInterval implements UpdateInterval<EmptyUpdateInterval> {
        @Override
        public EmptyUpdateInterval join(EmptyUpdateInterval otherUpdateInterval) {
            return null;
        }
    }
    
    /**
     * For a key and an optional update interval can compute a new value to be stored in the cache.
     * @author Axel Uhl (D043530)
     *
     * @param <K> the cache's key type
     * @param <V> the cache's value type
     * @param <U> the update interval type
     */
    public static interface CacheUpdater<K, V, U extends UpdateInterval<U>> {
        /**
         * Called by a background task to perform the potentially expensive update computations. The cache is
         * not updated with the results immediately. Instead, the result of this operation is later passed to
         * {@link #provideNewCacheValue(Object, Object)} with the cache entry for <code>key</code> locked for writing.
         */
        V computeCacheUpdate(K key, U updateInterval) throws Exception;
        
        /**
         * Expected to deliver an updated cache value quickly (compared to the potentially much more expensive
         * {@link #computeCacheUpdate(Object, UpdateInterval)} method which is run in a background task and doesn't lock
         * the cache for readers).
         * 
         * @param key
         *            the key for which to deliver the cache update
         * @param oldCacheValue
         *            the value associated with <code>key</code> up to now; may be <code>null</code>
         * @param computedCacheUpdate
         *            the result of {@link #computeCacheUpdate(Object, UpdateInterval)} called for <code>key</code>. A
         *            trivial implementation may simply return <code>computeCacheUpdate</code> if no further changes are
         *            required. However, an implementation may take the opportunity to update the result of
         *            {@link SmartFutureCache#get(Object, boolean)} called with <code>key</code> and <code>false</code>
         *            to obtain the current cache value for <code>key</code> and incrementally update it with
         *            <code>computedCacheUpdate</code> instead of constructing a new cache value which again may be
         *            fairly expensive.
         */
        V provideNewCacheValue(K key, V oldCacheValue, V computedCacheUpdate, U updateInterval);
    }

    public static abstract class AbstractCacheUpdater<K, V, U extends UpdateInterval<U>> implements CacheUpdater<K, V, U> {
        @Override
        public V provideNewCacheValue(K key, V oldCacheValue, V computedCacheUpdate, U updateInterval) {
            return computedCacheUpdate;
        }
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
    private static class FutureTaskWithCancelBlocking<V, U extends UpdateInterval<U>> extends FutureTask<V> {
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
    
    public SmartFutureCache(CacheUpdater<K, V, U> cacheUpdateComputer, String nameForLocks) {
        this.ongoingRecalculations = new ConcurrentHashMap<K, FutureTaskWithCancelBlocking<V, U>>();
        this.cache = new ConcurrentHashMap<K, V>();
        this.recalculator = Executors.newSingleThreadExecutor();
        this.cacheUpdateComputer = cacheUpdateComputer;
        this.locksForKeys = new ConcurrentHashMap<K, NamedReentrantReadWriteLock>();
        this.nameForLocks = nameForLocks;
    }
    
    private NamedReentrantReadWriteLock getOrCreateLockForKey(K key) {
        synchronized (locksForKeys) {
            NamedReentrantReadWriteLock result = locksForKeys.get(key);
            if (result == null) {
                result = new NamedReentrantReadWriteLock(nameForLocks+" for key "+key, /* fair */ false);
                locksForKeys.put(key, result);
            }
            return result;
        }
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
                                V preResult = cacheUpdateComputer.computeCacheUpdate(key, joinedUpdateInterval);
                                final NamedReentrantReadWriteLock lock = getOrCreateLockForKey(key);
                                LockUtil.lockForWrite(lock);
                                try {
                                    V result = cacheUpdateComputer.provideNewCacheValue(key, cache.get(key), preResult, joinedUpdateInterval);
                                    if (result == null) {
                                        cache.remove(key);
                                    } else {
                                        cache.put(key, result);
                                    }
                                    return result;
                                } finally {
                                    LockUtil.unlockAfterWrite(lock);
                                    ongoingRecalculations.remove(key);
                                }
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
    
    /**
     * Fetches a value for <code>key</code> from the cache. If no {@link #triggerUpdate(Object, UpdateInterval)} for the <code>key</code>
     * has ever happened, <code>null</code> will be returned. Otherwise, depending on <code>waitForLatest</code> the result is taken
     * from the cache straight away (<code>waitForLatest==false</code>) or, if a re-calculation for the <code>key</code> is still
     * ongoing, the result of that ongoing re-calculation is returned.
     */
    public V get(K key, boolean waitForLatest) {
        V value = null;
        if (waitForLatest) {
            FutureTaskWithCancelBlocking<V, U> future;
            synchronized (ongoingRecalculations) {
                // as long as we hold the lock on ongoingManeuverCacheRecalculations, the Futures contained in it are not cancelled
                future = ongoingRecalculations.get(key);
                if (future != null) {
                    future.dontCancel();
                } else {
                    value = cache.get(key);
                }
            }
            if (future != null) {
                try {
                    value = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.throwing(SmartFutureCache.class.getName(), "get", e);
                    throw new RuntimeException(e);
                }
            } // else no calculation currently going on; value has been fetched from latest cache entry
        } else {
            LockUtil.lockForRead(getOrCreateLockForKey(key));
            try {
                value = cache.get(key);
            } finally {
                LockUtil.unlockAfterRead(getOrCreateLockForKey(key));
            }
        }
        return value;
    }

    public Set<K> keySet() {
        return cache.keySet();
    }
}
