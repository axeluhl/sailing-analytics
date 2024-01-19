package com.sap.sse.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;

/**
 * If a non-trivial calculation may be requested multiple times by the same {@link Subject}, such that more requests are
 * received before the first calculation has completed, this cache helps avoiding redundant calculations by letting
 * subsequent overlapping requests wait for the completion of the ongoing calculation. Once all these requests have been
 * fulfilled by the first calculation, the cache entry for the subject is removed, hence the next request will trigger a
 * new calculation again.
 * <p>
 * 
 * This leads to a reduction of concurrent calculations for the same subject of usually only a single one.
 * <p>
 * 
 * The cache uses a {@link ConcurrentHashMap} as its cache. When requests arrive from multiple threads for the same
 * cache key, without additional synchronization it is possible that both threads don't find an entry for their cache
 * key, thus triggering multiple concurrent calculations. Later requests will then find any one ongoing calculation as
 * the others will have their cache entry overwritten. This implementation prefers the absence of strict and expensive
 * locking over avoiding the corner case of quasi-simultaneous requests from the same subject.
 * 
 * @param K
 *            the cache key component type which will be combined with the {@link Subject}'s
 *            {@link Subject#getPrincipal() principal's} name to form the complete key
 * 
 * @param V
 *            the value type to manage by this cache
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SingleCalculationPerSubjectCache<K, V> {
    private static final Logger logger = Logger.getLogger(SingleCalculationPerSubjectCache.class.getName());

    private final ConcurrentMap<Pair<K, String>, Future<V>> ongoingLiveCalculationsByRaceAndRequestingUsername;

    private final Function<K, V> functionCalculatingValueForKey;

    private final Duration timeoutBeforeCalculatingInPlace;

    /**
     * @param functionCalculatingValueForKey
     *            the function that calculates a value for a key; in most cases, concurrent execution of the function
     *            for the same {@link SecurityUtils#getSubject() current subject} and equal {@code key} is avoided by
     *            this cache, and calls to {@link #get(Object)} performed by the same subject with an equal key will
     *            wait for the execution currently ongoing to complete and then return its result. If the calculation
     *            fails with an exception, concurrent calls fail for the same exception.
     * @param timeoutBeforeCalculatingInPlace
     *            if not {@code null}, when a caller of {@link #get(Object)} has been waiting this long for a running
     *            calculation to complete without success, a new calculation is started in the thread calling
     *            {@link #get(Object)} instead.
     * 
     */
    public SingleCalculationPerSubjectCache(Function<K, V> functionCalculatingValueForKey, Duration timeoutBeforeCalculatingInPlace) {
        this.ongoingLiveCalculationsByRaceAndRequestingUsername = new ConcurrentHashMap<>();
        this.functionCalculatingValueForKey = functionCalculatingValueForKey;
        this.timeoutBeforeCalculatingInPlace = timeoutBeforeCalculatingInPlace;
    }
    
    /**
     * Computes the {@code functionComputingValueForKey} unless for the {@link SecurityUtils#getSubject() current subject}
     * an evaluation 
     */
    public V get(K key) {
        final Subject subject = SecurityUtils.getSubject();
        final String username = subject == null ? null : subject.getPrincipal() == null ? null : subject.getPrincipal().toString();
        final Pair<K, String> cacheKey = new Pair<>(key, username);
        V result;
        final Future<V> ongoingLiveCalculationForTrackedRace;
        if ((ongoingLiveCalculationForTrackedRace = ongoingLiveCalculationsByRaceAndRequestingUsername.get(cacheKey)) != null) {
            try {
                if (timeoutBeforeCalculatingInPlace == null) {
                    result = ongoingLiveCalculationForTrackedRace.get();
                } else {
                    result = ongoingLiveCalculationForTrackedRace.get(timeoutBeforeCalculatingInPlace.asMillis(), TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error computing function for key "+key, e);
            } catch (TimeoutException toe) {
                logger.warning("Timeout waiting for ongoing function evaluation for "+key+"; computing in place now");
                result = functionCalculatingValueForKey.apply(key);
            }
        } else {
            final CompletableFuture<V> ongoingCalculation = new CompletableFuture<>();
            ongoingLiveCalculationsByRaceAndRequestingUsername.put(cacheKey, ongoingCalculation);
            try {
                result = functionCalculatingValueForKey.apply(key);
                ongoingCalculation.complete(result);
            } catch (Exception e) {
                ongoingCalculation.completeExceptionally(e);
                throw e;
            } finally {
                ongoingLiveCalculationsByRaceAndRequestingUsername.remove(cacheKey);
            }
        }
        return result;
    }
}
