package com.sap.sse.gwt.client.async;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * Optimizes requests by trimming their wanted {@link TimeRange} against a cache of (fetched and still outstanding)
 * results. In the context of a {@link TimeRangeActionsExecutor} such a cache is responsible for managing the requests
 * for a single {@code Key}, such as a competitor or a boat class.<p>
 * 
 * Before making a request for a time range, the executor has to call {@link #trimAndRegisterRequest(TimeRange, boolean)}.
 * When {@code false} is used for the {@code forceTimeRange} parameter, this cache will 
 *
 * @param <Result>
 *            type of the results of the remote calls which are to be cached
 * @see TimeRangeActionsExecutor
 * @author Tim Hessenmüller (D062243)
 */
public class TimeRangeResultCache<Result> {
    private static final int TRIM_MAX_ITERATIONS = 20;
    private static final int CACHE_SIZE = 32;

    /**
     * Stores properties of a request that may have been the result of trimming an original request's time ranges.
     * If trimming took place (see {@link TimeRangeResultCache#trimAndRegisterRequest(TimeRange, boolean)}), the
     * trimmed request will have {@link #getDependencies() dependencies} on other {@link Request}s whose results
     * it needs to combine with its own result in order to satisfy the original untrimmed request from which it
     * was derived by trimming.<p>
     * 
     * This request keeps track of whether it {@link #hasResult() has received a result}. It furthermore manages
     * a set of {@link AsyncCallback} callbacks that will be triggered exactly once when a result has been
     * {@link #setResult(Pair) received} for this request.
     */
    protected class Request {
        private final Set<Request> dependsOnSet = new HashSet<>();
        private boolean hasResult = false;
        private Pair<TimeRange, Result> result = null;
        private final List<AsyncCallback<Void>> waitingOnResultList = new ArrayList<>(0); // TODO Verify initial cap

        public Request() {
        }

        public Request(Pair<TimeRange, Result> result) {
            this.hasResult = true;
            this.result = result;
        }

        /**
         * Obtains all other requests (excluding {@code this} request) of which all or parts of their results
         * are required to combine them with the results of this trimmed request into a result that reflects the
         * response to the original request from which this request was derived by trimming.
         */
        public Set<Request> getDependencies() {
            return dependsOnSet;
        }

        /**
         * Records that this request depends on the results of {@code request} because this request was trimmed based
         * on the assumption that {@code request}'s results are or will become available and cover a part of what this
         * request was originally expected to deliver.
         */
        public void addDependency(Request request) {
            dependsOnSet.add(request);
        }

        public void clearDependencies() {
            dependsOnSet.clear();
        }

        public Pair<TimeRange, Result> getResult() {
            return result;
        }

        /**
         * Sets the result for this request's total time range; {@link #hasResult()} will return {@code true}
         * after this method returns, and {@link #getResult()} will return {@code result} from then on.
         * All callbacks that were registered using {@link #registerOnResultCallback(AsyncCallback)} up to this time
         * will be {@link AsyncCallback#onSuccess(Object) notified}.
         */
        public void setResult(Pair<TimeRange, Result> result) {
            this.hasResult = true;
            this.result = result;
            waitingOnResultList.forEach(c -> c.onSuccess(null));
            waitingOnResultList.clear();
        }

        public boolean hasResult() {
            return hasResult;
        }

        /**
         * The callback is added to the set of callbacks notified when this request {@link #setResult(Pair) receives}
         * its result. Registering a callback after the result has already been received has no effect. The callback
         * set will be cleared once the result has been received.
         */
        public void registerOnResultCallback(AsyncCallback<Void> callback) {
            waitingOnResultList.add(callback);
        }
    }

    /**
     * The size-limited {@link Request} cache. The keys are the time ranges that may have resulted from trimming and
     * represent those time ranges for which requests are effectively made. The corresponding value {@link Request}
     * object captures {@link Request#getDependencies() requests on which the request depends} after having been
     * trimmed.
     */
    private final Map<TimeRange, Request> requestCache = new LinkedHashMap<TimeRange, Request>(
            /* initialCapacity */ CACHE_SIZE, /* loadFactor */ .75f, /* accessOrder */ true) {
        private static final long serialVersionUID = -5421470532158005911L;

        @Override
        protected boolean removeEldestEntry(Entry<TimeRange, Request> eldest) {
            return size() >= CACHE_SIZE; // FIXME don't remove an entry on whose request other requests in the cache depend
        }
    };

    /**
     * Trims the requested {@link TimeRange} against results that are already cached or requested and registers the
     * trimmed {@link TimeRange} with the cache as a request that will be made and that therefore shall already be used
     * to trim subsequent requests.
     *
     * @param toTrim
     *            {@link TimeRange} to request.
     * @param forceTimeRange
     *            if {@code false} the request will be optimized with cached results.
     * @return {@link TimeRange} to effectively request (potentially trimmed from {@code toTrim}) or {@code null} if no
     *         request is to be made since the results are cached.
     */
    TimeRange trimAndRegisterRequest(TimeRange toTrim, boolean forceTimeRange) {
        final Request request = new Request();
        final TimeRange effectivePotentiallyTrimmedRequest;
        if (!forceTimeRange) { // TODO Remove old results?
            effectivePotentiallyTrimmedRequest = trimTimeRangeAndAttachDeps(toTrim, request, requestCache.entrySet());
        } else {
            effectivePotentiallyTrimmedRequest = toTrim;
        }
        // null signals that the result is already present
        if (effectivePotentiallyTrimmedRequest != null) {
            // cache the request which is expected to not yet have a result
            requestCache.put(effectivePotentiallyTrimmedRequest, request);
        }
        return effectivePotentiallyTrimmedRequest;
    }

    /**
     * Registers that a request has returned with a result and collects all other needed cached results that are needed
     * to construct a complete, time-contiguous {@link Result}.
     *
     * @param effectiveRequestTimeRange
     *            {@link TimeRange} that the request that now returned a result was trimmed to.
     * @param result
     *            {@link Result} of the trimmed request; this result will be cached, keyed by its trimmed {@code timeRange}
     * @param callbackIfResultsAreMissing
     *            {@link AsyncCallback}{@code <Void>} to call {@link AsyncCallback#onSuccess(Object)} on once a request
     *            this one depends on and which was still in transit returns. Note that this then does not imply that all requests
     *            needed to fulfill the original request have delivered their result yet; the same may happen again for another
     *            dependency when trying again later.
     * @return {@link List} of {@link TimeRange}, {@link Result} {@link Pair}s that contain all data needed to construct
     *         a time-continuous {@link Result} object, or {@code null} if not all results on which the request for
     *         {@code effectiveRequestTimeRange} depended have been received yet; in this case the {@code callbackIfResultsAreMissing}
     *         will later receive a callback if one (but maybe not all) of those results has been received.
     */
    public List<Pair<TimeRange, Result>> registerAndCollectResult(TimeRange effectiveRequestTimeRange, Result result,
            AsyncCallback<Void> callbackIfResultsAreMissing) {
        final Pair<TimeRange, Result> requestResult = new Pair<>(effectiveRequestTimeRange, result);
        Request request = requestCache.get(effectiveRequestTimeRange);
        if (request != null) {
            request.setResult(requestResult); // notifies all callbacks that were waiting for this result
        } else {
            // FIXME the request that has returned now must have been evicted from the size-limited cache; but what about any dependencies?
            request = new Request(requestResult);
            requestCache.put(effectiveRequestTimeRange, request);
        }
        final List<Pair<TimeRange, Result>> results = new ArrayList<>(request.getDependencies().size() + 1);
        results.add(requestResult);
        for (final Request dep : request.getDependencies()) { // FIXME see above; no dependencies for new request that replaces an evicted one
            if (!dep.hasResult()) {
                dep.registerOnResultCallback(callbackIfResultsAreMissing);
                return null; // TODO it's sufficient to register on *one* dependency, although this may lead to a few premature re-tries
            } else {
                results.add(dep.getResult());
            }
        }
        // FIXME if ExecutorCallback resolves the same compound result multiple times, clearing dependencies here would no longer let us find the dependencies required here
        request.clearDependencies(); // FIXME moot if the request was just created above because the original one was evicted from the cache earlier
        return results;
    }

    /**
     * Removes the failed request from cache.
     *
     * @param timeRange
     *            effective {@link TimeRange} of failed request (result of potential trimming) 
     */
    public void registerFailure(TimeRange timeRange) {
        Request request = requestCache.remove(timeRange);
        // TODO Requests dependent on this one?
        // FIXME shouldn't this propagate as failure also to the callbacks of dependent requests? Add failure tests for trimmed requests!
        if (request != null) {
            request.setResult(null);
        }
    }

    /**
     * Trims a request based on cached results, returning a potentially trimmed time range that reflects which part(s)
     * is/are missing from the cache. For simplicity {@code toTrim} will currently not be split up into multiple
     * TimeRanges if a part in the middle is cached.<p>
     * 
     * 
     *
     * @param toTrim
     *            {@link TimeRange} to trim.
     * @param request
     *            {@link Request} to attach dependencies to that reflect of which other requests the results are
     *            required in order to fulfill the request for the complete {@code toTrim} time range
     * @param rangesToTrimWith
     *            {@link TimeRange}s that {@code toTrim} will be trimmed against.
     * @return {@link TimeRange} which to request because it is not covered by {@code rangesToTrimWith} or {@code null}
     *         if no request to the server is to be made.
     */
    private TimeRange trimTimeRangeAndAttachDeps(TimeRange toTrim, Request request,
            Iterable<Map.Entry<TimeRange, Request>> rangesToTrimWith) {
        List<Map.Entry<TimeRange, Request>> rangesToTrimWithAsList = new LinkedList<>();
        rangesToTrimWith.forEach(rangesToTrimWithAsList::add);
        for (int i = 0; i < TRIM_MAX_ITERATIONS; i++) {
            boolean rangeWasTrimmedThisIteration = false;
            final Iterator<Map.Entry<TimeRange, Request>> iter = rangesToTrimWithAsList.iterator();
            while (iter.hasNext()) {
                final Map.Entry<TimeRange, Request> element = iter.next();
                final TimeRange trimWith = element.getKey();
                if (toTrim.intersects(trimWith)) {
                    // Only consider TimeRanges that touch or overlap toTrim
                    if (toTrim.liesWithin(trimWith)) {
                        // toTrim is fully covered by trimWith
                        if (request != null) {
                            request.addDependency(element.getValue());
                        }
                        return null; // meaning that no additional effective requests are required; the cache has or will have it all
                    } else if (!toTrim.includes(trimWith)
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
                    // no intersection between request and cache entry; remove the cache entry from the list of requests to consider
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
