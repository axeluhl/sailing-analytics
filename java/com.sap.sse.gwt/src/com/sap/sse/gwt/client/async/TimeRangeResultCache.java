package com.sap.sse.gwt.client.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * Optimizes requests by trimming their wanted {@link TimeRange} against a cache of (fetched and still outstanding)
 * results. In the context of a {@link TimeRangeActionsExecutor} such a cache is responsible for managing the requests
 * for a single {@code Key}, such as a competitor or a boat class.
 * <p>
 * 
 * Before making a request for a time range, the executor has to call
 * {@link #trimAndRegisterRequest(TimeRange, boolean)}. When {@code false} is used for the {@code forceTimeRange}
 * parameter, this cache will
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
     * Stores properties of a request that may have been the result of trimming an original request's time ranges. If
     * trimming took place (see {@link TimeRangeResultCache#trimAndRegisterRequest(TimeRange, boolean)}), the trimmed
     * request will have {@link #getChildrenSet() dependencies} on other {@link Request}s whose results it needs to
     * combine with its own result in order to satisfy the original untrimmed request from which it was derived by
     * trimming.
     * <p>
     *
     * This request keeps track of whether it {@link #hasResult() has received a result}. It furthermore manages a set
     * of {@link AsyncCallback} callbacks that will be triggered exactly once when a result has been
     * {@link #setResult(Pair) received} for this request.
     * <p>
     *
     * Dependent requests are represented as a petri net. {@link Request}s are vertices and {@link #childrenSet} and
     * {@link #parentSet} make up doubly-linked edges. {@link Result}s are tokens which get handled by a transition
     * function implemented in {@link #notifyActionSuccessIfHasAllResults()}.
     */
    protected class Request {
        private final TimeRangeAsyncAction<?, ?> action;
        private final AsyncCallback<List<Pair<TimeRange, Result>>> callback;
        private boolean callbackWasCalled = false;
        private final TimeRange actionTimeRange;
        private TimeRange trimmedTimeRange;
        private final Set<Request> parentSet = new HashSet<>();
        private final Set<Request> childrenSet = new HashSet<>();
        private int childrenWithoutResultCounter = 0;
        private boolean hasResult = false;
        private Result result = null;

        public Request(TimeRange timeRange, AsyncCallback<List<Pair<TimeRange, Result>>> callback,
                TimeRangeAsyncAction<?, ?> action) {
            this.actionTimeRange = timeRange;
            this.trimmedTimeRange = timeRange;
            this.callback = callback;
            this.action = action;
        }

        /**
         * Obtains all other requests (excluding {@code this} request) of which all or parts of their results are
         * required to combine them with the results of this trimmed request into a result that reflects the response to
         * the original request from which this request was derived by trimming.
         */
        public Set<Request> getChildrenSet() {
            return childrenSet;
        }

        /**
         * Might immediately call {@link #notifyActionSuccessIfHasAllResults()}.
         */
        public void addChildren(List<Request> requests) {
            if (callbackWasCalled) {
                throw new IllegalStateException("Children may not be added after results have been submitted!");
            }
            childrenWithoutResultCounter++; // This will later be undone and serves the purpose of delaying the call to notifyActionSuccessIfHasAllResults until all children have been added
            for (Request request : requests) {
                assert !this.equals(request);
                final boolean notPresentBefore = childrenSet.add(request);
                if (notPresentBefore) {
                    childrenWithoutResultCounter++;
                }
                request.addParent(this); // Will immediately decrement childrenWithoutResultCounter if result is present
            }
            childrenWithoutResultCounter--;
            notifyActionSuccessIfHasAllResults();
        }

        private void releaseChildren() {
            for (Request child : childrenSet) {
                child.removeParent(this);
                if (child.canBeEvicted()) {
                    TimeRangeResultCache.this.evictRequestFromCache(child);
                }
            }
            childrenSet.clear();
        }

        public Set<Request> getParentSet() {
            return parentSet;
        }

        private void addParent(Request parent) {
            assert !this.equals(parent);
            final boolean notPresentBefore = parentSet.add(parent);
            if (notPresentBefore && hasResult) {
                parent.onChildSuccess();
            }
        }

        private void removeParent(Request parent) {
            parentSet.remove(parent);
        }

        public void onSuccess(Result result) {
            setResult(result);
            notifyActionSuccessIfHasAllResults();
            parentSet.forEach(parent -> parent.onChildSuccess());
        }

        public void onFailure(Throwable caught) {
            notifyActionFailure(caught);
            Set<Request> parents = new HashSet<>(parentSet); //TODO ConcurrentModificationError
            parents.forEach(parent -> parent.onChildFailure(caught));
        }

        private void onChildSuccess() {
            childrenWithoutResultCounter--;
            notifyActionSuccessIfHasAllResults();
        }

        private void onChildFailure(Throwable caught) {
            // TODO
            notifyActionFailure(caught);
        }

        private void notifyActionSuccessIfHasAllResults() {
            if (!callbackWasCalled && hasResult && childrenWithoutResultCounter <= 0) {
                // TODO
                List<Pair<TimeRange, Result>> results = new ArrayList<>(childrenSet.size() + 1);
                results.add(new Pair<>(getTrimmedTimeRange(), getResult()));
                for (Request child : childrenSet) {
                    results.add(new Pair<>(child.getTrimmedTimeRange(), child.getResult()));
                }
                callback.onSuccess(results);
                callbackWasCalled = true;
                releaseChildren();
                if (canBeEvicted()) {
                    TimeRangeResultCache.this.evictRequestFromCache(this);
                }
            }
        }

        private void notifyActionFailure(Throwable caught) {
            //TODO
            if (!callbackWasCalled) {
                callback.onFailure(caught);
                callbackWasCalled = true;
                releaseChildren();
                if (canBeEvicted()) {
                    TimeRangeResultCache.this.evictRequestFromCache(this);
                }
            }
        }

        protected boolean canBeEvicted() {
            return callbackWasCalled && parentSet.isEmpty();
        }

        public Result getResult() {
            return result;
        }

        /**
         * Sets the result for this request's total time range; {@link #hasResult()} will return {@code true} after this
         * method returns, and {@link #getResult()} will return {@code result} from then on. All callbacks that were
         * registered using {@link #registerOnResultCallback(AsyncCallback)} up to this time will be
         * {@link AsyncCallback#onSuccess(Object) notified}.
         */
        private void setResult(Result result) {
            this.hasResult = true;
            this.result = result;
        }

        public boolean hasResult() {
            return hasResult;
        }

        public TimeRange getActionTimeRange() {
            return actionTimeRange;
        }

        public TimeRange getTrimmedTimeRange() {
            return trimmedTimeRange;
        }

        public void setTrimmedTimeRange(TimeRange timeRange) {
            this.trimmedTimeRange = timeRange;
        }
    }

    /**
     * The size-limited {@link Request} cache. The keys are the time ranges that may have resulted from trimming and
     * represent those time ranges for which requests are effectively made. The corresponding value {@link Request}
     * object captures {@link Request#getChildrenSet() requests on which the request depends} after having been trimmed.
     */
    private final Map<TimeRangeAsyncAction<?, ?>, Request> requestCache = new HashMap<>(CACHE_SIZE);

    private void evictRequestFromCache(TimeRangeAsyncAction<?, ?> action) {
        requestCache.remove(action);
    }

    private void evictRequestFromCache(Request request) {
        evictRequestFromCache(request.action);
    }

    int getCacheSize() {
        return requestCache.size();
    }

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
    public TimeRange trimAndRegisterRequest(TimeRange toTrim, boolean forceTimeRange, TimeRangeAsyncAction<?, ?> action,
            AsyncCallback<List<Pair<TimeRange, Result>>> callback) {
        final Request request = new Request(toTrim, callback, action);
        final TimeRange effectivePotentiallyTrimmedRequest = !forceTimeRange ? trimTimeRangeAndAttachDeps(request)
                : toTrim;
        // null signals that the result is already present
        if (effectivePotentiallyTrimmedRequest != null) {
            // cache the request which is expected to not yet have a result
            requestCache.put(action, request);
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
     *            {@link Result} of the trimmed request; this result will be cached, keyed by its trimmed
     *            {@code timeRange}
     * @param callbackIfResultsAreMissing
     *            {@link AsyncCallback}{@code <Void>} to call {@link AsyncCallback#onSuccess(Object)} on once a request
     *            this one depends on and which was still in transit returns. Note that this then does not imply that
     *            all requests needed to fulfill the original request have delivered their result yet; the same may
     *            happen again for another dependency when trying again later.
     * @return {@link List} of {@link TimeRange}, {@link Result} {@link Pair}s that contain all data needed to construct
     *         a time-continuous {@link Result} object, or {@code null} if not all results on which the request for
     *         {@code effectiveRequestTimeRange} depended have been received yet; in this case the
     *         {@code callbackIfResultsAreMissing} will later receive a callback if one (but maybe not all) of those
     *         results has been received.
     */
    public void registerResult(TimeRangeAsyncAction<?, ?> action, Result result) {
        Request request = requestCache.get(action);
        if (request == null) {
            throw new IllegalArgumentException("Attempted to register result for non-existent request: " + action.toString());
        }
        request.onSuccess(result); // notifies all callbacks that were waiting for this result
    }

    /**
     * Removes the failed request from cache.
     *
     * @param timeRange
     *            effective {@link TimeRange} of failed request (result of potential trimming)
     */
    public void registerFailure(TimeRangeAsyncAction<?, ?> action, Throwable cause) {
        Request request = requestCache.get(action);
        if (request != null) {
            request.onFailure(cause);
        }
    }

    /**
     * Trims a request based on cached results, returning a potentially trimmed time range that reflects which part(s)
     * is/are missing from the cache. For simplicity {@code toTrim} will currently not be split up into multiple
     * TimeRanges if a part in the middle is cached.
     * <p>
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
    private TimeRange trimTimeRangeAndAttachDeps(Request request) {
        TimeRange toTrim = request.getActionTimeRange();
        List<Request> rangesToTrimWithAsList = new LinkedList<>(); //TODO Profile against ArrayList (swap and remove)
        requestCache.values().forEach(rangesToTrimWithAsList::add);
        List<Request> childrenList = new ArrayList<>();
        iterationsLoop: for (int i = 0; i < TRIM_MAX_ITERATIONS; i++) {
            boolean rangeWasTrimmedThisIteration = false;
            final Iterator<Request> iter = rangesToTrimWithAsList.iterator();
            while (iter.hasNext()) {
                final Request element = iter.next();
                final TimeRange trimWith = element.getTrimmedTimeRange();
                if (trimWith != null) {
                    final boolean rangeAfter = toTrim.from().after(trimWith.to()) || toTrim.from().equals(trimWith.to());
                    final boolean fromAfter = toTrim.from().after(trimWith.from());
                    final boolean fromIncluded = fromAfter || toTrim.from().equals(trimWith.from());
                    final boolean rangeBefore = toTrim.to().before(trimWith.from()) || toTrim.to().equals(trimWith.from());
                    final boolean toBefore = toTrim.to().before(trimWith.to());
                    final boolean toIncluded = toBefore || toTrim.to().equals(trimWith.to());
                    if (rangeAfter || rangeBefore) {
                        // toTrim and trimWith do not touch at all; Do not consider in following iterations 
                        iter.remove();
                    } else if (fromIncluded && toIncluded) {
                        // toTrim is completely included in trimWith
                        childrenList.add(element);
                        toTrim = null;
                        break iterationsLoop; // toTrim has been completely trimmed away
                    } else if (fromIncluded || toIncluded) {
                        // toTrim overlaps trimWith but only on one side i.e. trimWith is not included in toTrim
                        childrenList.add(element);
                        toTrim = toTrim.subtract(trimWith).iterator().next();
                        iter.remove();
                        rangeWasTrimmedThisIteration = true;
                    }
                } else {
                    // trimWith is null; Do not consider in following iterations
                    iter.remove();
                }
            }
            if (!rangeWasTrimmedThisIteration) {
                break iterationsLoop;
            }
        }
        request.setTrimmedTimeRange(toTrim);
        request.addChildren(childrenList);
        return toTrim;
    }
}
