package com.sap.sailing.util.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.sap.sailing.util.impl.SmartFutureCache.UpdateInterval;

/**
 * A cache for which a background update can be triggered. Readers can decide whether they want to wait for any ongoing
 * background update or read the latest cached value for a key. An update trigger can provide an optional parameter for
 * the update which may, e.g., control the interval of the cached value to update. When an update is triggered and
 * another update is already running, the update is queued. If there already is an update queued for the same key, the
 * optional update parameters are {@link UpdateInterval#join(UpdateInterval) "joined"} (for example, the two update
 * intervals are joined to form one interval which incorporates both original update intervals).
 * <p>
 * 
 * A {@link CacheUpdater} needs to be passed to the constructor which carries out the actual calculation whose values
 * are to be cached. The {@link CacheUpdater} interface assumes that a cache update may be computed in two steps: first,
 * a value is computed for a key and an update interval which may be computationally expensive. Then, in a second step,
 * the new value is combined with the previous cache value for the same key and update interval. The default
 * implementation of {@link CacheUpdater#provideNewCacheValue(Object, Object, Object, UpdateInterval)} simply returns
 * the <code>computedCacheUpdate</code> parameter which is the result computed by
 * {@link CacheUpdater#computeCacheUpdate(Object, UpdateInterval)} before.
 * <p>
 * 
 * The cache only knows about results computed based on a {@link #triggerUpdate(Object, UpdateInterval)} call. The
 * {@link #get(Object, boolean)} method itself will not trigger a computation if no cache value exists for the request.
 * Therefore, the {@link #triggerUpdate(Object, UpdateInterval)} calls need to ensure that all data expected to be
 * managed by this cache---specifically the area spanned by the update interval---is covered.
 * <p>
 * 
 * There may be situations, such as during a start-up phase, where the automatic and immediate re-calculation is not
 * desirable, particularly because during such a phase the number of re-calculations scheduled perhaps by far outweighs
 * the number of {@link #get(Object, boolean)} requests. In such a phase it is smarter to suspend the automatic
 * re-calculation and defer it until a {@link #get(Object, boolean)} request actually happens. For this purpose,
 * the {@link #suspend} and {@link #resume} methods can be used. No matter the suspend/resume state, the {@link #get(Object, boolean)}
 * method will always respond in line with the {@link #triggerUpdate(Object, UpdateInterval)} calls, only that re-calculations
 * are not immediately started when in suspended mode, and {@link #get(Object, boolean) get(key, false)} will no trigger a
 * re-calculation at all. When resuming, any pending recalculations triggered so far are scheduled for immediate execution such
 * that subsequent {@link #get(Object, boolean) get(key, true)} calls will wait for their completion.
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
    
    /**
     * Note that this needs to have more than one thread because there may be calculations used for cache updates that
     * need to wait for other cache updates to finish. If those were all to be handled by a single thread, deadlocks
     * would occur. Remember that there may still be single-core machines, so the factor with which
     * <code>availableProcessors</code> is multiplied needs to be greater than one at least.
     */
    private final static Executor recalculator = new ThreadPoolExecutor(/* corePoolSize */ 0,
            /* maximumPoolSize */ 3*Runtime.getRuntime().availableProcessors(),
            /* keepAliveTime */ 60, TimeUnit.SECONDS,
            /* workQueue */ new LinkedBlockingQueue<Runnable>());

    private final CacheUpdater<K, V, U> cacheUpdateComputer;
    
    private final Map<K, NamedReentrantReadWriteLock> locksForKeys;
    
    private final String nameForLocks;
    
    /**
     * See {@link #suspend} and {@link #resume}.
     */
    private boolean suspended;
    
    private final Map<K, U> triggeredWhileSuspended;
    
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
         * Called by a background task to perform the potentially expensive update computations. The cache is not
         * updated with the results immediately. Instead, the result of this operation is later passed to
         * {@link #provideNewCacheValue(Object, Object)} with the cache entry for <code>key</code> locked for writing.
         * 
         * @param updateInterval
         *            if <code>null</code>, the result must reflect the entire current data on which the cache is based,
         *            like the "infinite" interval
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
        this.cacheUpdateComputer = cacheUpdateComputer;
        this.locksForKeys = new ConcurrentHashMap<K, NamedReentrantReadWriteLock>();
        this.nameForLocks = nameForLocks;
        this.triggeredWhileSuspended = new HashMap<K, U>();
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
    
    public void suspend() {
        synchronized (ongoingRecalculations) {
            suspended = true;
        }
    }
    
    public void resume() {
        synchronized (ongoingRecalculations) {
            suspended = false;
            for (Iterator<Map.Entry<K, U>> i=triggeredWhileSuspended.entrySet().iterator(); i.hasNext(); ) {
                Entry<K, U> e = i.next();
                triggerUpdate(e.getKey(), e.getValue());
                i.remove();
            }
        }
    }
    
    /**
     * Triggers a cache update for <code>key</code> for the <code>updateInterval</code> specified. If a re-calculation
     * for this key is already scheduled, this method will try to cancel it, but that may not work because the
     * task has already started. In any case, the new task is scheduled. Note, that the {@link #recalculator}'s
     * queue is most likely an ordered, FIFO-like queue. Therefore, when triggering many updates for many keys,
     * the corresponding re-calculations will be dispatched to the next available thread from the thread pool
     * in the order of the {@link #triggerUpdate(Object, UpdateInterval)} calls. If a large sequence of keys
     * needs frequent updating, but re-calculation doesn't keep up, make sure the keys are passed in randomized
     * order. Otherwise, the keys towards the end will never have their values re-calculated because their
     * tasks will keep getting cancelled before started.
     */
    public void triggerUpdate(final K key, U updateInterval) {
        // establish and maintain the following invariant: after lock on ongoingManeuverCacheRecalculations is released,
        // no Future contained in it is in cancelled state
        synchronized (ongoingRecalculations) {
            final U oldUpdateInterval;
            final U joinedUpdateInterval;
            if (suspended) {
                oldUpdateInterval = triggeredWhileSuspended.get(key);
            } else {
                FutureTaskWithCancelBlocking<V, U> oldFuture = ongoingRecalculations.get(key);
                if (oldFuture != null) {
                    oldFuture.cancel(/* mayInterruptIfRunning */false);
                }
                oldUpdateInterval = oldFuture == null ? null : oldFuture.getUpdateInterval();
            }
            if (oldUpdateInterval == null) {
                joinedUpdateInterval = updateInterval;
            } else {
                if (updateInterval == null) {
                    joinedUpdateInterval = oldUpdateInterval;
                } else {
                    joinedUpdateInterval = updateInterval.join(oldUpdateInterval);
                }
            }
            if (suspended) {
                triggeredWhileSuspended.put(key, joinedUpdateInterval);
            } else {
                createAndExecuteRecalculation(key, joinedUpdateInterval, /* callerWaitsSynchronouslyForResult */ false);
            }
        }
    }

    /**
     * Creates a {@link FutureTask} for the (re-)calculation of the cache entry for <code>key</code> across update
     * interval <code>joinedUpdateInterval</code>, enters it into {@link #ongoingRecalculations} and schedules its
     * execution with {@link #recalculator}. The method synchronizes on {@link #ongoingRecalculations}.
     * 
     * @param callerWaitsSynchronouslyForResult
     *            if <code>true</code>, this allows the future to assume the caller's locks. See also
     *            {@link LockUtil#propagateLockSetFrom(Thread)}. This can be helpful to avoid read-read deadlocks
     *            in conjunction with fair {@link ReentrantReadWriteLock}s.
     */
    private void createAndExecuteRecalculation(final K key, final U joinedUpdateInterval,
            final boolean callerWaitsSynchronouslyForResult) {
        final Thread callerThread = Thread.currentThread();
        synchronized (ongoingRecalculations) {
            final FutureTaskWithCancelBlocking<V, U> future;
            future = new FutureTaskWithCancelBlocking<V, U>(new Callable<V>() {
                @Override
                public V call() {
                    try {
                        if (callerWaitsSynchronouslyForResult) {
                            LockUtil.propagateLockSetFrom(callerThread);
                        }
                        try {
                            V preResult = cacheUpdateComputer.computeCacheUpdate(key, joinedUpdateInterval);
                            final NamedReentrantReadWriteLock lock = getOrCreateLockForKey(key);
                            LockUtil.lockForWrite(lock);
                            try {
                                V result = cacheUpdateComputer.provideNewCacheValue(key, cache.get(key), preResult,
                                        joinedUpdateInterval);
                                cache(key, result);
                                return result;
                            } finally {
                                LockUtil.unlockAfterWrite(lock);
                                ongoingRecalculations.remove(key);
                            }
                        } finally {
                            if (callerWaitsSynchronouslyForResult) {
                                LockUtil.unpropagateLockSetFrom(callerThread);
                            }
                        }
                    } catch (Exception e) {
                        // cache won't be updated
                        logger.throwing(SmartFutureCache.class.getName(), "triggerUpdate", e);
                        throw new RuntimeException(e);
                    }
                }
            }, joinedUpdateInterval);
            ongoingRecalculations.put(key, future);
            recalculator.execute(future);
        }
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
                if (suspended && waitForLatest) {
                    final boolean wasTriggeredWhileSuspended = triggeredWhileSuspended.containsKey(key); // update interval may have deliberately been null
                    if (wasTriggeredWhileSuspended) {
                        // If suspended and the caller wants to get the latest results, and during suspend recalculations were
                        // triggered, and execute them now.
                        // This will enter the new future into ongoingRecalculations, and therefore this method
                        // will wait for its completion below.
                        // It is OK to assert that this method will synchronously wait for the results before
                        // releasing any locks held by the current thread because
                        // createAndExecuteRecalculation adds key to ongoingRecalculations, and
                        // further down this method, the future will be fetched from ongoingRecalculations
                        // with that same key, and a get will be performed on that future.
                        createAndExecuteRecalculation(key, triggeredWhileSuspended.remove(key),
                                /* callerWaitsSynchronouslyForResult */ true);
                    }
                }
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

    protected void cache(final K key, V value) {
        if (value == null) {
            cache.remove(key);
        } else {
            cache.put(key, value);
        }
    }
}
