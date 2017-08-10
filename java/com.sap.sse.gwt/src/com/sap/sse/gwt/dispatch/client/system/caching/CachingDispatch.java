package com.sap.sse.gwt.dispatch.client.system.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.shared.caching.HasClientCacheTotalTimeToLive;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * Delegating dispatch implementation that caches results if actions implement IsClientCache interface.
 * 
 */
public final class CachingDispatch<CTX extends DispatchContext> implements DispatchSystemAsync<CTX> {
    private static final Logger LOG = Logger.getLogger(CachingDispatch.class.getName());
    private final CacheCleanupTask invalidationTask;
    private final HashMap<String, ResultHolder> resultsCache = new HashMap<>();
    private final DispatchSystemAsync<CTX> dispatch;
    private final int defaultTimeToLiveMillis;

    /**
     * Create new caching dispatch instance with default values, in particular a default time to live
     * for cache entries of three minutes.
     * 
     * @param service
     *            the underlying service used to make dispatch calls
     */
    public CachingDispatch(DispatchSystemAsync<CTX> service) {
        this(service, true, (int) Duration.ONE_MINUTE.times(3).asMillis());
    }

    /**
     * Create new caching dispatch instance with given parameters
     * 
     * @param service
     *            the underlying service used to make dispatch calls
     * @param enableCleanup
     *            enable cleanup cached elements
     * @param defaultTimeToLive
     *            default cached element time to live
     */
    public CachingDispatch(DispatchSystemAsync<CTX> service, boolean enableCleanup, int defaultTimeToLive) {
        this.dispatch = service;
        this.defaultTimeToLiveMillis = defaultTimeToLive;
        if (enableCleanup) {
            invalidationTask = new CacheCleanupTask();
        } else {
            invalidationTask = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Result, A extends Action<R, CTX>> void execute(A action, AsyncCallback<R> callback) {
        if (action instanceof IsClientCacheable) {
            final IsClientCacheable clientCacheableAction = (IsClientCacheable) action;
            final ResultHolder cachedResult = resultsCache.get(key(clientCacheableAction));
            if (cachedResult != null && cachedResult.isValid()) {
                LOG.fine("Cache hit for " + action.getClass().getName());
                callback.onSuccess((R) cachedResult.getPayload());
            } else {
                LOG.fine("Cache miss for " + action.getClass().getName());
                executeAndCache(action, callback);
            }
        } else {
            LOG.fine("Action is not cacheable: " + action.getClass().getName());
            dispatch.execute(action, callback);
        }
    };

    /**
     * Perform dispatch call with using an intercepting callback for client cacheable commands.
     * 
     * @param action
     * @param callback
     */
    private <R extends Result, A extends Action<R, CTX>> void executeAndCache(final A action,
            final AsyncCallback<R> callback) {
        if (action instanceof IsClientCacheable) {
            dispatch.execute(action, new InterceptingCallback<>(action, callback));
        } else {
            dispatch.execute(action, callback);
        }
    }

    /**
     * Intercepting callback that stores the result into cache.
     */
    private class InterceptingCallback<R extends Result, A extends Action<R, CTX>> implements AsyncCallback<R> {
        private final A action;
        private final AsyncCallback<R> callback;

        public InterceptingCallback(A action, AsyncCallback<R> callback) {
            this.action = action;
            this.callback = callback;
        }

        @Override
        public void onSuccess(R result) {
            final IsClientCacheable clientCacheableAction = (IsClientCacheable) action;
            final String instanceKey = key(clientCacheableAction);
            int cacheTotalTimeToLiveMillis = timeToLive(action, result);
            resultsCache.put(instanceKey, new ResultHolder(System.currentTimeMillis() + cacheTotalTimeToLiveMillis,
                    result));
            LOG.finest("Added " + instanceKey + " to cache, ttl: " + cacheTotalTimeToLiveMillis + "ms");
            if (invalidationTask != null && !invalidationTask.isRunning()) {
                LOG.finest("Schedule cache cleanup");
                invalidationTask.schedule((int) Duration.ONE_MINUTE.times(5).asMillis());
            }
            callback.onSuccess(result);
        }

        /**
         * Calculate time to live fore result
         */
        private int timeToLive(A action, R result) {
            int cacheTotalTimeToLiveMillis = defaultTimeToLiveMillis;
            if (result instanceof HasClientCacheTotalTimeToLive) {
                cacheTotalTimeToLiveMillis = ((HasClientCacheTotalTimeToLive) result).cacheTotalTimeToLiveMillis();
            } else if (action instanceof HasClientCacheTotalTimeToLive) {
                cacheTotalTimeToLiveMillis = ((HasClientCacheTotalTimeToLive) action).cacheTotalTimeToLiveMillis();
            }
            return cacheTotalTimeToLiveMillis;
        }
        @Override
        public void onFailure(Throwable caught) {
            callback.onFailure(caught);
        }
    }

    /**
     * Generate instance key for given action.
     */
    private String key(IsClientCacheable action) {
        StringBuilder key = new StringBuilder(action.getClass().getName()).append("_");
        action.cacheInstanceKey(key);
        return key.toString();
    }

    /**
     * Holder class used to store results in cache.
     */
    private static class ResultHolder {
        final Result payload;
        final long invalidationTimestamp;

        public ResultHolder(long invalidationTimestamp, Result payload) {
            this.invalidationTimestamp = invalidationTimestamp;
            this.payload = payload;
        }

        public boolean isValid() {
            return System.currentTimeMillis() < invalidationTimestamp;
        }

        public Result getPayload() {
            return payload;
        }
    }

    public void cleanupExpiredItems() {
        final ArrayList<String> keys = new ArrayList<String>(resultsCache.keySet());
        LOG.finest("Start cache cleanup");
        for (String key : keys) {
            if (!resultsCache.get(key).isValid()) {
                resultsCache.remove(key);
            }
        }
    }

    public int ttlItemsInCache() {
        return resultsCache.size();
    }

    /**
     * Removes due elements from resultscache.
     * 
     */
    private class CacheCleanupTask extends Timer {
        @Override
        public void run() {
            try {
                cleanupExpiredItems();
            } finally {
                if (!resultsCache.isEmpty()) {
                    invalidationTask.schedule((int) Duration.ONE_MINUTE.times(5).asMillis());
                }
            }
        }
    }
}
