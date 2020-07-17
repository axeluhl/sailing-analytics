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
 *            Type returned by the combined call. See {@link TimeRangeAsyncAction}. Holds a compound result for several
 *            keys, such as for several competitors or several boats. A {@link TimeRangeAsyncCallback} object is
 *            expected to be able to {@link TimeRangeAsyncCallback#unzipResult(Object) split} such a {@code Result}
 *            object into the {@code Key}s and {@code SubResult}s, as well as to
 *            {@link TimeRangeAsyncCallback#joinSubResults(TimeRange, List) join} these sub-results of the individual
 *            results for trimmed requests into a sub-result for the full time range requested for one {@code Key}.
 * @param <SubResult>
 *            Type representing an individual part or channel of a compound {@link Result}, specific to one {@code Key},
 *            as obtained by {@link TimeRangeAsyncCallback#unzipResult(Object) splitting} a compound {@code Result}.
 * @param <Key>
 *            Type used to index {@link SubResult}s.
 * @see TimeRangeAsyncAction
 * @see TimeRangeAsyncCallback
 *
 * @author Tim Hessenmüller (D062243)
 */
public class TimeRangeActionsExecutor<Result, SubResult, Key> {
    /**
     * Callback called by {@link TimeRangeActionsExecutor#executor} upon receiving an answer from the server
     * for a potentially trimmed request
     */
    private final class ExecutorCallback implements AsyncCallback<Result> {
        private final TimeRangeAsyncAction<Result, Key> action;
        private final TimeRangeAsyncCallback<Result, SubResult, Key> callback;
        private final Map<Key, TimeRange> requestedTimeRangeMap;
        private final Map<Key, List<Pair<TimeRange, SubResult>>> subResultsMap;

        private ExecutorCallback(TimeRangeAsyncAction<Result, Key> action,
                Collection<Pair<Key, TimeRange>> requestedTimeRanges,
                TimeRangeAsyncCallback<Result, SubResult, Key> callback) {
            this.action = action;
            this.callback = callback;
            this.requestedTimeRangeMap = new HashMap<>(requestedTimeRanges.size());
            requestedTimeRanges.forEach(pair -> requestedTimeRangeMap.put(pair.getA(), pair.getB()));
            this.subResultsMap = new HashMap<>(requestedTimeRanges.size());
        }

        @Override
        public void onSuccess(Result result) {
            // from the compound result extract the potentially trimmed sub-results per key
            final Map<Key, SubResult> unzippedResultMap = result != null ? callback.unzipResult(result) : Collections.emptyMap();
            for (Key key : requestedTimeRangeMap.keySet()) {
                final SubResult trimmedSubResultForKey = unzippedResultMap.get(key);
                final TimeRangeResultCache<SubResult> cache = getSubResultCache(key);
                cache.registerResult(action, trimmedSubResultForKey);
            }
        }

        @Override
        public void onFailure(Throwable caught) {
            requestedTimeRangeMap.keySet().forEach(key -> getSubResultCache(key).registerFailure(action, caught));
            callback.onFailure(caught);
        }

        public void onSubResultSuccess(Key key, List<Pair<TimeRange, SubResult>> results) {
            subResultsMap.put(key, results);
            if (subResultsMap.size() == requestedTimeRangeMap.size()) {
                final Map<Key, SubResult> unzippedResult = new HashMap<>(subResultsMap.size());
                for (Map.Entry<Key, List<Pair<TimeRange, SubResult>>> entry : subResultsMap.entrySet()) {
                    TimeRange requestedTimeRange = requestedTimeRangeMap.get(entry.getKey());
                    unzippedResult.put(entry.getKey(), callback.joinSubResults(requestedTimeRange, entry.getValue()));
                }
                callback.onSuccess(callback.zipSubResults(unzippedResult));
            }
        }

        public void onSubResultFailure(Throwable caught) {
            //TODO
        }
    }

    /**
     * {@link AsyncActionsExecutor} that will be used to actually execute remote procedures.
     */
    private final AsyncActionsExecutor executor;

    /**
     * Action category passed along to the {@link #executor}.
     *
     * @see AsyncActionsExecutor#execute(AsyncAction, String, AsyncCallback)
     */
    private final String actionCategory;

    private final Map<Key, TimeRangeResultCache<SubResult>> cacheMap = new HashMap<>(); //TODO Invalidation

    /**
     * Creates a new executor using a default action category {@link MarkedAsyncCallback#CATEGORY_GLOBAL}.
     */
    public TimeRangeActionsExecutor(AsyncActionsExecutor actionsExecutor) {
        this(actionsExecutor, MarkedAsyncCallback.CATEGORY_GLOBAL);
    }

    public TimeRangeActionsExecutor(AsyncActionsExecutor actionsExecutor, String actionCategory) {
        this.executor = Objects.requireNonNull(actionsExecutor);
        this.actionCategory = actionCategory;
    }

    /**
     * Executes a {@link TimeRangeAsyncAction} and returns the results to a {@link TimeRangeAsyncCallback}. Calls
     * {@link #execute(TimeRangeAsyncAction, AsyncCallback, boolean)} with {@code forceTimeRange} set to {@code false},
     * thus allowing for cached requests to help trim the new request so as to avoid overlapping redundant requests.
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
     *            if {@code false} the request will be optimized by trimming its time range; otherwise it will be
     *            executed unchanged
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
        final Collection<Pair<Key, TimeRange>> requestedTimeRanges = action.getTimeRanges();
        final ExecutorCallback execCallback = new ExecutorCallback(action, requestedTimeRanges, callback);
        final List<Pair<Key, TimeRange>> trimmedTimeRanges = new ArrayList<>(requestedTimeRanges.size());
        for (Pair<Key, TimeRange> part : requestedTimeRanges) {
            final TimeRangeResultCache<SubResult> cache = getSubResultCache(part.getA());
            TimeRange potentiallyTrimmedTimeRange = cache.trimAndRegisterRequest(part.getB(), forceTimeRange, action,
                    new AsyncCallback<List<Pair<TimeRange,SubResult>>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            execCallback.onSubResultFailure(caught);
                        }
                        @Override
                        public void onSuccess(List<Pair<TimeRange, SubResult>> result) {
                            execCallback.onSubResultSuccess(part.getA(), result);
                        }
            });
            if (potentiallyTrimmedTimeRange != null) {
                trimmedTimeRanges.add(new Pair<>(part.getA(), potentiallyTrimmedTimeRange));
            }
        }
        executor.execute(cb -> action.execute(trimmedTimeRanges, cb), actionCategory, execCallback);
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
