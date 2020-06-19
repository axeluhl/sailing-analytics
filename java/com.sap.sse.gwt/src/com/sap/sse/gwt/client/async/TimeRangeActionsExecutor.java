package com.sap.sse.gwt.client.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * An Executor for efficient, asynchronous execution of {@link TimeRangeAsyncAction}s. For the actual execution this
 * class relies on an {@link AsyncActionsExecutor}.<p>
 *
 *
 * The idea is to provide an abstraction layer which will handle caching and intelligently cut down on the number and
 * size of request being made to the server.
 *
 * @param <Result>
 *            Type returned by remote procedure. See {@link TimeRangeAsyncAction}.
 * @param <SubResult>
 *            Type representing an individual part or channel of a complete {@link Result}.
 * @param <Key>
 *            Typed used to index {@link SubResult}s.
 *
 * @author Tim Hessenmüller (D062243)
 */
public class TimeRangeActionsExecutor<Result, SubResult, Key> {
    /**
     * Callback called by {@link TimeRangeActionsExecutor#executor} upon receiving an answer from the server.
     */
    private final class ExecutorCallback implements AsyncCallback<Result> {
        private final TimeRangeAsyncCallback<Result, SubResult, Key> callback;
        private final Collection<Pair<Key, TimeRange>> requestedTimeRanges;
        private final Map<Key, TimeRange> trimmedTimeRangeMap;

        private ExecutorCallback(Collection<Pair<Key, TimeRange>> requestedTimeRanges,
                List<Pair<Key, TimeRange>> trimmedTimeRanges, TimeRangeAsyncCallback<Result, SubResult, Key> callback) {
            this.callback = callback;
            this.requestedTimeRanges = requestedTimeRanges;
            trimmedTimeRangeMap = new HashMap<>(trimmedTimeRanges.size());
            trimmedTimeRanges.forEach(pair -> trimmedTimeRangeMap.put(pair.getA(), pair.getB()));
        }

        @Override
        public void onSuccess(Result result) {
            Map<Key, SubResult> unzippedResultMap = result != null ? callback.unzipResults(result)
                    : Collections.emptyMap();
            Map<Key, Pair<TimeRange, SubResult>> completedResultMap = new HashMap<>();
            for (Pair<Key, TimeRange> request : requestedTimeRanges) {
                SubResult subResult = unzippedResultMap.get(request.getA());
                TimeRangeResultCache<SubResult> cache = getSubResultCache(request.getA());
                List<Pair<TimeRange, SubResult>> partialResults = cache.registerAndCollectResult(
                        trimmedTimeRangeMap.get(request.getA()), subResult, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void voidResult) {
                                ExecutorCallback.this.onSuccess(result);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                            }
                        });
                if (partialResults == null) {
                    return; // A required request is still in transit
                }
                SubResult completedSubResult = callback.joinSubResults(request.getB(), partialResults);
                completedResultMap.put(request.getA(), new Pair<>(request.getB(), completedSubResult));
            }
            callback.onSuccess(completedResultMap);
        }

        @Override
        public void onFailure(Throwable caught) {
            trimmedTimeRangeMap.entrySet().forEach(e -> getSubResultCache(e.getKey()).registerFailure(e.getValue()));
            callback.onFailure(caught);
        }
    }

    /**
     * {@link AsyncActionsExecutor} that will be used to actually execute remote procedures.
     */
    protected final AsyncActionsExecutor executor;
    /**
     * Action category passed along to the {@link #executor}.
     *
     * @see AsyncActionsExecutor#execute(AsyncAction, String, AsyncCallback)
     */
    protected String actionCategory = MarkedAsyncCallback.CATEGORY_GLOBAL;
    protected final Map<Key, TimeRangeResultCache<SubResult>> cacheMap = new HashMap<>(); //TODO Invalidation

    public TimeRangeActionsExecutor(AsyncActionsExecutor actionsExecutor) {
        this.executor = Objects.requireNonNull(actionsExecutor);
    }

    public TimeRangeActionsExecutor(AsyncActionsExecutor actionsExecutor, String actionCategory) {
        this.executor = Objects.requireNonNull(actionsExecutor);
        this.actionCategory = actionCategory;
    }

    /**
     * Executes a {@link TimeRangeAsyncAction} and returns the results to a {@link TimeRangeAsyncCallback}. Calls
     * {@link #execute(TimeRangeAsyncAction, AsyncCallback, boolean)} with {@code forceTimeRange} set to {@code false}.
     *
     * @param action
     *            {@link TimeRangeAsyncAction} to execute.
     * @param callback
     *            {@link TimeRangeAsyncCallback} to return results to.
     * @see #execute(TimeRangeAsyncAction, TimeRangeAsyncCallback, boolean)
     */
    public void execute(TimeRangeAsyncAction<Result, Key> action,
            TimeRangeAsyncCallback<Result, SubResult, Key> callback) {
        execute(action, callback, /* forceTimeRange */ false);
    }

    /**
     * Executes a {@link TimeRangeAsyncAction} and returns the results to a {@link TimeRangeAsyncCallback}.
     *
     * @param action
     *            {@link TimeRangeAsyncAction} to execute.
     * @param callback
     *            {@link TimeRangeAsyncCallback} to return results to.
     * @param forceTimeRange
     *            if {@code false} the request will be optimized.
     * @see #execute(TimeRangeAsyncAction, TimeRangeAsyncCallback)
     */
    public void execute(TimeRangeAsyncAction<Result, Key> action,
            TimeRangeAsyncCallback<Result, SubResult, Key> callback, boolean forceTimeRange) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null!");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null!");
        }
        List<Pair<Key, TimeRange>> trimmedTimeRanges = new ArrayList<>(action.getTimeRanges().size());
        for (Pair<Key, TimeRange> part : action.getTimeRanges()) {
            TimeRangeResultCache<SubResult> cache = getSubResultCache(part.getA());
            TimeRange trimmedRange = cache.trimAndRegisterRequest(part.getB(), forceTimeRange);
            if (trimmedRange != null) {
                trimmedTimeRanges.add(new Pair<>(part.getA(), trimmedRange));
            }
        }
        if (!trimmedTimeRanges.isEmpty()) {
            executor.execute(new AsyncAction<Result>() {
                @Override
                public void execute(AsyncCallback<Result> callback) {
                    action.execute(trimmedTimeRanges, callback);
                }
            }, actionCategory, new ExecutorCallback(action.getTimeRanges(), trimmedTimeRanges, callback));
        } else {
            new ExecutorCallback(action.getTimeRanges(), trimmedTimeRanges, callback).onSuccess(null);
        }
    }

    private TimeRangeResultCache<SubResult> getSubResultCache(Key key) {
        TimeRangeResultCache<SubResult> cache = cacheMap.get(key);
        if (cache == null) {
            cache = new TimeRangeResultCache<>();
            cacheMap.put(key, cache);
        }
        return cache;
    }
}
