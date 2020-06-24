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
 * An Executor for efficient, asynchronous execution of {@link TimeRangeAsyncAction}s. This executor operates on
 * {@link TimeRangeAsyncAction}s which specify the wanted {@link TimeRange} by {@link Key} with
 * {@link TimeRangeAsyncAction#getTimeRanges()} and are expected to return a {@code Result}. The {@link Key} could,
 * e.g., be a competitor or a boat class.
 * <p>
 * 
 * This executor manages and optimizes concurrent requests for the same {@code Key} that have overlapping time ranges.
 * This way, redundant requests for the same data can be avoided. This executor collects the results of the overlapping
 * concurrent requests and uses them to satisfy each individual client request as if they had all been executed in their
 * entirety.
 * <p>
 * 
 * Each of the {@link TimeRange}s is then trimmed against the {@link TimeRaneResultCache cache} of returned and
 * outstanding requests for the respective {@code Key}. After having received the server responses for all time ranges
 * required to satisfy a {@link TimeRangeAsyncAction} the trimmed results are combined and the
 * {@link TimeRangeAsyncCallback callback} is invoked.
 * <p>
 * 
 * For the actual execution this class relies on an {@link AsyncActionsExecutor}.
 * <p>
 *
 *
 * @param <Result>
 *            Type returned by the combined call. See {@link TimeRangeAsyncAction}.
 * @param <SubResult>
 *            Type representing an individual part or channel of a complete {@link Result}, as returned by
 *            an individual remote procedure call.
 * @param <Key>
 *            Type used to index {@link SubResult}s.
 * @see TimeRangeAsyncAction
 * @see TimeRangeAsyncCallback
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
                final Key key = request.getA();
                SubResult subResult = unzippedResultMap.get(key);
                TimeRangeResultCache<SubResult> cache = getSubResultCache(key);
                List<Pair<TimeRange, SubResult>> partialResults = cache.registerAndCollectResult(
                        trimmedTimeRangeMap.get(key), subResult, new AsyncCallback<Void>() {
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
                completedResultMap.put(key, new Pair<>(request.getB(), completedSubResult));
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
