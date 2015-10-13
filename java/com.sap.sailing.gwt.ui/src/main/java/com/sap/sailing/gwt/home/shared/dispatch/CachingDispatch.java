package com.sap.sailing.gwt.home.shared.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sse.common.Duration;

/**
 * Delegating dispatch implementation that caches results if actions implement IsClientCache interface.
 * 
 * @author pgtaboada
 */
public final class CachingDispatch implements DispatchAsync {

    private static final Logger LOG = Logger.getLogger(CachingDispatch.class.getName());
    private final int DEFAULT_TIME_TO_LIVE_MILLIS = (int) Duration.ONE_SECOND.asMillis() * 30;
    private final CacheCleanupTask invalidationTask = new CacheCleanupTask();
    private final HashMap<String, ResultHolder> resultsCache = new HashMap<>();
    private final DispatchAsync dispatch;

    public CachingDispatch(DispatchAsync service) {
        this.dispatch = service;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Result, A extends Action<R>> void execute(A action, AsyncCallback<R> callback) {
        if (action instanceof IsClientCacheable) {
            final IsClientCacheable clientCacheableAction = (IsClientCacheable) action;
            final ResultHolder cachedResult = resultsCache.get(key(clientCacheableAction));
            if (cachedResult != null) {
                if (cachedResult.isValid()) {
                    LOG.fine("Cache hit for " + action.getClass().getName());
                    callback.onSuccess((R) cachedResult.getPayload());
                } else {
                    executeAndCache(action, callback);
                }
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
     * Perform dispatch call with intercepting callback.
     * 
     * @param action
     * @param callback
     */
    private <R extends Result, A extends Action<R>> void executeAndCache(final A action, final AsyncCallback<R> callback) {
        dispatch.execute(action, new InterceptingCallback<>(action, callback));
    }

    /**
     * Intercepting callback that stores the result into cache.
     * 
     * @author pgtaboada
     *
     * @param <R>
     * @param <A>
     */
    private class InterceptingCallback<R extends Result, A extends Action<R>> implements AsyncCallback<R> {
        private final A action;
        private final AsyncCallback<R> callback;

        public InterceptingCallback(A action, AsyncCallback<R> callback) {
            this.action = action;
            this.callback = callback;
        }

        @Override
        public void onSuccess(R result) {
            if (action instanceof IsClientCacheable) {
                final IsClientCacheable clientCacheableAction = (IsClientCacheable) action;
                final String instanceKey = key(clientCacheableAction);
                int cacheTotalTimeToLiveMillis = DEFAULT_TIME_TO_LIVE_MILLIS;
                if (result instanceof HasClientCacheTotalTimeToLive) {
                    cacheTotalTimeToLiveMillis = ((HasClientCacheTotalTimeToLive) result).cacheTotalTimeToLiveMillis();
                } else if (action instanceof HasClientCacheTotalTimeToLive) {
                    cacheTotalTimeToLiveMillis = ((HasClientCacheTotalTimeToLive) action).cacheTotalTimeToLiveMillis();
                }
                resultsCache.put(key(clientCacheableAction), new ResultHolder(System.currentTimeMillis()
                        + cacheTotalTimeToLiveMillis, result));
                LOG.finest("Added " + instanceKey + " to cache, ttl: " + cacheTotalTimeToLiveMillis + "ms");
                if (!invalidationTask.isRunning()) {
                    LOG.finest("Schedule cache cleanup");
                    invalidationTask.schedule((int) Duration.ONE_MINUTE.asMillis());
                }
            }
            callback.onSuccess(result);
        }

        @Override
        public void onFailure(Throwable caught) {
            callback.onFailure(caught);
        }
    }

    /**
     * Generate instance key for given action.
     * 
     * @param action
     * @return
     */
    private String key(IsClientCacheable action) {
        StringBuilder key = new StringBuilder(action.getClass().getName()).append("_");
        action.cacheInstanceKey(key);
        return key.toString();
    }

    /**
     * Holder class used to store results in cache.
     * 
     * @author pgtaboada
     *
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

    /**
     * Removes due elements from resultscache.
     * 
     * @author pgtaboada
     *
     */
    private class CacheCleanupTask extends Timer {
        @Override
        public void run() {
            try {
                LOG.finest("Start cache cleanup");
                final ArrayList<String> keys = new ArrayList<String>(resultsCache.keySet());
                for (String key : keys) {
                    if (!resultsCache.get(key).isValid()) {
                        resultsCache.remove(key);
                    }
                }
            } finally {
                if (!resultsCache.isEmpty()) {
                    invalidationTask.schedule((int) Duration.ONE_MINUTE.asMillis());
                }
            }
        }
    }
}
