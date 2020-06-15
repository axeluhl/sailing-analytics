package com.sap.sse.gwt.client.async;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * Optimizes requests by trimming their wanted {@link TimeRange} against a cache of (fetched and still outstanding)
 * results.
 *
 * @param <Result>
 *            Type to be cached.
 * @see TimeRangeActionsExecutor
 * @author Tim Hessenmüller (D062243)
 */
public class TimeRangeResultCache<Result> {
    private static final int TRIM_MAX_ITERATIONS = 20;
    private static final int CACHE_SIZE = 32;

    protected class Request<T> {
        private final Set<Request<T>> dependsOnSet = new HashSet<>();
        private boolean hasResult = false;
        private Pair<TimeRange, T> result = null;
        private final List<AsyncCallback<Void>> waitingOnResultList = new ArrayList<>(0); // TODO Verify initial cap

        public Request() {
        }

        public Request(Pair<TimeRange, T> result) {
            this.hasResult = true;
            this.result = result;
        }

        public Set<Request<T>> getDependencies() {
            return dependsOnSet;
        }

        public void addDependency(Request<T> request) {
            dependsOnSet.add(request);
        }

        public void clearDependencies() {
            dependsOnSet.clear();
        }

        public Pair<TimeRange, T> getResult() {
            return result;
        }

        public void setResult(Pair<TimeRange, T> result) {
            this.hasResult = true;
            this.result = result;
            waitingOnResultList.forEach(c -> c.onSuccess(null));
            waitingOnResultList.clear();
        }

        public boolean hasResult() {
            return hasResult;
        }

        public void registerOnResultCallback(AsyncCallback<Void> callback) {
            waitingOnResultList.add(callback);
        }
    }

    private final Map<TimeRange, Request<Result>> requestCache = new LinkedHashMap<TimeRange, Request<Result>>(
            /* initialCapacity */ CACHE_SIZE, /* loadFactor */ .75f, /* accessOrder */ true) {
        private static final long serialVersionUID = 1L; // TODO Generated id is 1?

        @Override
        protected boolean removeEldestEntry(Entry<TimeRange, Request<Result>> eldest) {
            return size() >= CACHE_SIZE; // TODO
        }
    };

    /**
     * Trims the requested {@link TimeRange} against results that already cached and registers the trimmed
     * {@link TimeRange} with the cache.
     *
     * @param toTrim
     *            {@link TimeRange} to request.
     * @param forceTimeRange
     *            if {@code false} the request will be optimized with cached results.
     * @return {@link TimeRange} to request or {@code null} if no request is to be made since the results are cached.
     */
    public TimeRange trimAndRegisterRequest(TimeRange toTrim, boolean forceTimeRange) {
        Request<Result> request = new Request<>();
        if (!forceTimeRange) { // TODO Remove old results?
            toTrim = trimTimeRangeAndAttachDeps(toTrim, request, requestCache.entrySet());
        }
        // Signal result already present
        if (toTrim == null) {
            return null;
        }
        requestCache.put(toTrim, request);
        return toTrim;
    }

    /**
     * Registers that a request has returned with a result and collects all other needed cached results that are needed
     * to construct a complete, time-continuous {@code Result}.
     *
     * @param timeRange
     *            {@link TimeRange} that this request was trimmed to.
     * @param result
     *            {@code Result} of this request to cache.
     * @param callbackIfResultsAreMissing
     *            {@link AsyncCallback}{@code <Void>} to call {@link AsyncCallback#onSuccess(Object)} on once a request
     *            this one depends on and which was still in transit returns.
     * @return {@link List} of {@link TimeRange}, {@code Result} {@link Pair}s that contain all data needed to construct
     *         a time-continuous {@code Result} object.
     */
    public List<Pair<TimeRange, Result>> registerAndCollectResult(TimeRange timeRange, Result result,
            AsyncCallback<Void> callbackIfResultsAreMissing) {
        Pair<TimeRange, Result> requestResult = new Pair<>(timeRange, result);
        Request<Result> request = requestCache.get(timeRange);
        if (request != null) {
            request.setResult(requestResult);
        } else {
            request = new Request<>(requestResult);
            requestCache.put(timeRange, request);
        }

        List<Pair<TimeRange, Result>> results = new ArrayList<>(request.getDependencies().size() + 1);
        results.add(requestResult);
        for (Request<Result> dep : request.getDependencies()) {
            if (!dep.hasResult()) {
                dep.registerOnResultCallback(callbackIfResultsAreMissing);
                return null;
            }
            results.add(dep.getResult());
        }
        request.clearDependencies();
        return results;
    }

    /**
     * Removes the failed request from cache.
     *
     * @param timeRange
     *            {@link TimeRange} of failed request.
     */
    public void registerFailure(TimeRange timeRange) {
        Request<Result> request = requestCache.remove(timeRange);
        // TODO Requests dependent on this one?
        if (request != null) {
            request.setResult(null);
        }
    }

    /**
     * Trims a potential request with cached results. For simplicity {@code toTrim} will currently not be split up into
     * multiple TimeRanges if a part in the middle is cached.
     *
     * @param toTrim
     *            {@link TimeRange} to trim.
     * @param request
     *            {@link Request} to attach dependencies to.
     * @param rangesToTrimWith
     *            {@link TimeRange}s that {@code toTrim} will be trimmed against.
     * @return {@link TimeRange} which to request from the server because it is not covered by {@code rangesToTrimWith}
     *         or {@code null} if no request to the server is to be made.
     */
    private TimeRange trimTimeRangeAndAttachDeps(TimeRange toTrim, Request<Result> request,
            Iterable<Map.Entry<TimeRange, Request<Result>>> rangesToTrimWith) {
        List<Map.Entry<TimeRange, Request<Result>>> rangesToTrimWithList = new ArrayList<>();
        rangesToTrimWith.forEach(rangesToTrimWithList::add);
        for (int i = 0; i < TRIM_MAX_ITERATIONS; i++) {
            boolean rangeWasTrimmedThisIteration = false;
            Iterator<Map.Entry<TimeRange, Request<Result>>> iter = rangesToTrimWithList.iterator();
            while (iter.hasNext()) {
                final Map.Entry<TimeRange, Request<Result>> element = iter.next();
                final TimeRange trimWith = element.getKey();
                if (toTrim.intersects(trimWith)) {
                    // Only consider TimeRanges that touch or overlap toTrim
                    if (toTrim.liesWithin(trimWith)) {
                        // toTrim is fully covered by trimWith
                        if (request != null) {
                            request.addDependency(element.getValue());
                        }
                        return null;
                    }
                    if (!toTrim.includes(trimWith)
                            || (toTrim.from().equals(trimWith.from()) || toTrim.to().equals(trimWith.to()))) {
                        // toTrim and trimWith overlap and trimWith does not lie within toTrim
                        // OR special case where trimWith lies within toTrim but touches one of the ends
                        if (request != null) {
                            request.addDependency(element.getValue());
                        }
                        toTrim = toTrim.subtract(trimWith).iterator().next();
                        iter.remove();
                        rangeWasTrimmedThisIteration = true;
                    }
                } else {
                    iter.remove();
                }
            }
            if (!rangeWasTrimmedThisIteration) {
                break;
            }
        }
        return toTrim;
    }
}
