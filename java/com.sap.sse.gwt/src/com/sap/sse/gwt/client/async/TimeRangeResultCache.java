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
 * {@link #trimAndRegisterRequest(TimeRange, boolean)}.
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
     * Dependent requests are represented as a <b>Petri Net</b>. {@link Request}s are vertices and {@link #childrenSet}
     * and {@link #parentSet} make up doubly-linked edges. {@link Result}s are tokens which get handled by a transition
     * function implemented in {@link #notifyActionSuccessIfHasAllResults()}.
     * <p>
     *
     * To keep things organized a "child" {@link Request} will never act on its own but always notify its "parent"
     * {@link Request}s to act on its behalf. For this reason most methods are written from the "parents" perspective.
     * <p>
     *
     * This request keeps track of whether it {@link #hasResult() has received a result}. It furthermore manages a set
     * of {@link AsyncCallback} {@link #callback} that will be triggered <b>exactly once</b> when a result has been
     * {@link #setResult(Pair) received} for this request.
     */
    protected class Request {
        private final TimeRangeAsyncAction<?, ?> action;
        private final AsyncCallback<Void> callback;
        private boolean callbackWasCalled = false;
        private final TimeRange actionTimeRange;
        private TimeRange trimmedTimeRange;
        private final Set<Request> parentSet = new HashSet<>();
        private final Set<Request> childrenSet = new HashSet<>();
        private int childrenWithoutResultCounter = 0;
        private boolean hasResult = false;
        private Result result = null;
        private boolean actionResultRetrieved = false;

        public Request(TimeRange timeRange, AsyncCallback<Void> callback, TimeRangeAsyncAction<?, ?> action) {
            this.actionTimeRange = timeRange;
            this.trimmedTimeRange = timeRange;
            this.callback = callback;
            this.action = action;
        }

        /**
         * Obtains all other requests (excluding {@code this} request) of which all or parts of their results are
         * required to combine them with the results of this trimmed request into a result that reflects the response to
         * the original request {@link #action} from which this request was derived by trimming.
         */
        public Set<Request> getChildrenSet() {
            return childrenSet;
        }

        /**
         * Adds a {@link List} of {@link Request}s to this one indicating that this {@link Request} is dependent on
         * their {@link Result}s.
         * <p>
         * Might immediately call {@link #notifyActionSuccessIfHasAllResults()} if all children have their results.
         *
         * @throws IllegalStateException
         *             if invoked after {@link #callback} has been called.
         */
        public void addChildren(List<Request> requests) throws IllegalStateException {
            if (callbackWasCalled) {
                throw new IllegalStateException("Children may not be added after results have been submitted!");
            }
            childrenWithoutResultCounter++; // This will later be undone and serves the purpose of delaying the call to
                                            // notifyActionSuccessIfHasAllResults until all children have been added
            for (Request request : requests) {
                if (this.equals(request)) {
                    throw new IllegalArgumentException("Cannot add itself as child!");
                }
                final boolean notPresentBefore = childrenSet.add(request);
                if (notPresentBefore) {
                    childrenWithoutResultCounter++;
                }
                request.addParent(this); // Will immediately decrement childrenWithoutResultCounter if result is present
            }
            childrenWithoutResultCounter--;
            notifyActionSuccessIfHasAllResults();
        }

        /**
         * Clears {@link #childrenSet} and evicts the removed {@link Request}s from
         * {@link TimeRangeResultCache#requestCache} if they are no longer needed.
         */
        private void releaseChildren() {
            for (Request child : childrenSet) {
                child.removeParent(this);
                if (child.canBeEvicted()) {
                    TimeRangeResultCache.this.evictRequestFromCache(child);
                }
            }
            childrenSet.clear();
        }

        /**
         * Obtains all {@link Request}s that still depend on this one. Those {@link Request}s will be notified by this
         * one once it receives its {@link Result}.
         */
        public Set<Request> getParentSet() {
            return parentSet;
        }

        /**
         * Adds a new parent to {@link #parentSet}.
         *
         * @param parent
         *            {@link Request} to add as a parent. Must not be {@code this}.
         */
        private void addParent(Request parent) {
            if (this.equals(parent)) {
                throw new IllegalArgumentException("Cannot add itself as parent!");
            }
            final boolean notPresentBefore = parentSet.add(parent);
            if (notPresentBefore && hasResult) {
                parent.onChildSuccess();
            }
        }

        private void removeParent(Request parent) {
            parentSet.remove(parent);
        }

        /**
         * This method is to be called if {@code this} {@link Request} has received its {@link Result}. This
         * {@link Request} will subsequently notify its parent {@link Request}s.
         * <p>
         * If all children have their {@link Result}s the {@link #callback} will be notified.
         *
         * @param result
         *            this {@link Request}'s {@link Result}
         */
        public void onSuccess(Result result) {
            setResult(result);
            notifyActionSuccessIfHasAllResults();
            parentSet.forEach(parent -> parent.onChildSuccess());
        }

        /**
         * This method is to be called if an error occurred while getting {@code this} {@link Request}'s {@link Result}.
         * After calling the {@link #callback} the parents will be notified.
         *
         * @param caught
         *            {@link Throwable} that occurred
         */
        public void onFailure(Throwable caught) {
            notifyActionFailure(caught);
            HashSet<Request> parents = new HashSet<>(parentSet); // onChildFailure will remove elements from parentSet
            parents.forEach(parent -> parent.onChildFailure(caught));
        }

        /**
         * To be called by a child when it receives it's {@link Result} via {@link #onSuccess(Object)}.
         */
        private void onChildSuccess() {
            childrenWithoutResultCounter--;
            notifyActionSuccessIfHasAllResults();
        }

        /**
         * To be called by a child when it encounters an error and is informed of this in {@link #onFailure(Throwable)}.
         *
         * @param caught
         *            {@link Throwable} that occurred
         */
        private void onChildFailure(Throwable caught) {
            notifyActionFailure(new Throwable("Child encountered an error", caught));
        }

        /**
         * Notifies {@link #callback} <b>once</b> if all needed {@link Result}s are available.<p>
         * Implements the transition function used by this Petri Net.
         */
        private void notifyActionSuccessIfHasAllResults() {
            if (!callbackWasCalled && hasResult && childrenWithoutResultCounter <= 0) {
                callbackWasCalled = true;
                callback.onSuccess(null);
            }
        }

        /**
         * Notifies {@link #callback} of an error that occurred.
         *
         * @param caught
         *            {@link Throwable} that was encountered.
         */
        private void notifyActionFailure(Throwable caught) {
            if (!callbackWasCalled) {
                callbackWasCalled = true;
                callback.onFailure(caught);
                releaseChildrenAndEvictSelf();
            }
        }

        /**
         * Determines if this {@link Request} can be evicted from the cache.
         * @return {@code true} if it can be evicted
         */
        protected boolean canBeEvicted() {
            return callbackWasCalled && parentSet.isEmpty();
        }

        /**
         * Calls {@link #releaseChildren()} and then removes itself from the cache if no longer needed.
         */
        protected void releaseChildrenAndEvictSelf() {
            releaseChildren();
            callbackWasCalled = true;
            if (canBeEvicted()) {
                TimeRangeResultCache.this.evictRequestFromCache(this);
            }
        }

        /**
         * Collects and returns the needed {@link Result}s to complete the {@link #action}.
         *
         * @throws IllegalStateException
         *             if the callback has not been notified yet or if the results are being retrieved a second time
         */
        public List<Pair<TimeRange, Result>> getActionResult() throws IllegalStateException {
            if (!callbackWasCalled) {
                throw new IllegalStateException("Callback was not called yet");
            }
            if (actionResultRetrieved) {
                throw new IllegalStateException(
                        "Results have been retrieved already and the dependencies have been released");
            }
            List<Pair<TimeRange, Result>> actionResults = new ArrayList<>(childrenSet.size() + 1);
            actionResults.add(new Pair<>(getTrimmedTimeRange(), getResult()));
            for (Request child : childrenSet) {
                actionResults.add(new Pair<>(child.getTrimmedTimeRange(), child.getResult()));
            }
            releaseChildrenAndEvictSelf();
            actionResultRetrieved = true;
            return actionResults;
        }

        /**
         * Obtains the {@link Result} of this {@link Request}.
         * @return {@code null} if {@link #hasResult()} is {@code false}
         */
        public Result getResult() {
            return result;
        }

        /**
         * Sets the result for this request's total time range; {@link #hasResult()} will return {@code true} after this
         * method returns, and {@link #getResult()} will return {@code result} from then on.
         */
        private void setResult(Result result) {
            this.hasResult = true;
            this.result = result;
        }

        /**
         * @return {@code true} if a {@link Result} has been set.
         */
        public boolean hasResult() {
            return hasResult;
        }

        /**
         * @return {@code true} if {@link #onFailure(Throwable)} has been called.
         */
        public boolean hasFailed() {
            return callbackWasCalled && !hasResult;
        }

        /**
         * Obtains the {@link TimeRange} originally requested by the {@link #action}.
         */
        public TimeRange getActionTimeRange() {
            return actionTimeRange;
        }

        /**
         * Obtains the {@link TimeRange} that {@link #getActionTimeRange()} was trimmed to due to other {@link Request}s
         * in cache.
         */
        public TimeRange getTrimmedTimeRange() {
            return trimmedTimeRange;
        }

        /**
         * Sets the trimmed {@link TimeRange}.
         *
         * @param timeRange
         *            trimmed {@link TimeRange}
         */
        public void setTrimmedTimeRange(TimeRange timeRange) {
            this.trimmedTimeRange = timeRange;
        }
    }

    /**
     * The {@link Request} cache. The keys are the actions for which requests are effectively made. The corresponding
     * value {@link Request} object captures {@link Request#getChildrenSet() requests on which the request depends}
     * after having been trimmed.
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
     *            {@link TimeRange} to request
     * @param forceTimeRange
     *            if {@code false} the request will be optimized with cached results
     * @param action
     *            {@link TimeRangeAsyncAction} that this request belongs to
     * @param callback
     *            {@link AsyncCallback} which will be called exactly once when all needed {@link Result}s are ready.<br>
     *            The {@link Result}s can be retrieved using {@link #getResults(TimeRangeAsyncAction)}.
     * @return {@link TimeRange} to effectively request (potentially trimmed from {@code toTrim}) or {@code null} if no
     *         request is to be made since the results are cached.
     */
    public TimeRange trimAndRegisterRequest(TimeRange toTrim, boolean forceTimeRange, TimeRangeAsyncAction<?, ?> action,
            AsyncCallback<Void> callback) {
        final Request request = new Request(toTrim, callback, action);
        final TimeRange effectivePotentiallyTrimmedRequest = !forceTimeRange ? trimTimeRangeAndAttachDeps(request)
                : toTrim;
        // null signals that the result is already present
        // cache the request which is expected to not yet have a result
        requestCache.put(action, request);
        return effectivePotentiallyTrimmedRequest;
    }

    /**
     * Registers that a request has returned with a result and collects all other needed cached results that are needed
     * to construct a complete, time-contiguous {@link Result}.
     *
     * @param action
     *            {@link TimeRangeAsyncAction} that the request corresponds to
     * @param result
     *            {@link Result} of the trimmed request; this result will be cached
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
     * @param action
     *            corresponding {@link TimeRangeAsyncAction}
     * @param cause
     *            {@link Throwable} that occurred
     */
    public void registerFailure(TimeRangeAsyncAction<?, ?> action, Throwable cause) {
        Request request = requestCache.get(action);
        if (request != null) {
            request.onFailure(cause);
        }
    }

    /**
     * Retrieves the needed {@link Result}s to answer the {@code action}.
     * <p>
     * This method is <b>only to be called once</b> after the {@link AsyncCallback} passed to
     * {@link #trimAndRegisterRequest(TimeRange, boolean, TimeRangeAsyncAction, AsyncCallback)} was called indicating
     * that the {@link Result}s are ready.
     *
     * @param action
     *            corresponding {@link TimeRangeAsyncAction}
     * @return {@link Pair}s of {@link TimeRange}s and {@link Result}s which together cover the {@link TimeRange}
     *         requested by {@code action}
     */
    public List<Pair<TimeRange, Result>> getResults(TimeRangeAsyncAction<?, ?> action) throws IllegalArgumentException {
        Request request = requestCache.get(action);
        if (request == null) {
            throw new IllegalArgumentException("No request found for action: " + action.toString());
        }
        return request.getActionResult();
    }

    /**
     * Informs a {@link Request} that its {@link Result}s are no longer needed.
     *
     * @param action
     *            corresponding {@link TimeRangeAsyncAction}
     */
    public void removeRequest(TimeRangeAsyncAction<?, ?> action) throws IllegalArgumentException {
        Request request = requestCache.get(action);
        if (request == null) {
            throw new IllegalArgumentException("No request found for action: " + action.toString());
        }
        request.releaseChildrenAndEvictSelf();
    }

    /**
     * Trims a request based on cached results, returning a potentially trimmed time range that reflects which part(s)
     * is/are missing from the cache. For simplicity {@code toTrim} will currently not be split up into multiple
     * TimeRanges if a part in the middle is cached.
     *
     * @param request
     *            {@link Request} to trim and attach dependencies to that reflect of which other requests the results
     *            are required in order to fulfill the request
     * @return {@link TimeRange} which to request because it is not covered by {@code rangesToTrimWith} or {@code null}
     *         if no request to the server is to be made.
     */
    private TimeRange trimTimeRangeAndAttachDeps(Request request) {
        //TODO There is a lot of potential for improvements here
        TimeRange toTrim = request.getActionTimeRange();
        List<Request> rangesToTrimWithAsList = new LinkedList<>(requestCache.values());
        List<Request> childrenList = new ArrayList<>();
        iterationsLoop: for (int i = 0; i < TRIM_MAX_ITERATIONS; i++) {
            boolean rangeWasTrimmedThisIteration = false;
            final Iterator<Request> iter = rangesToTrimWithAsList.iterator();
            while (iter.hasNext()) {
                final Request element = iter.next();
                final TimeRange trimWith = element.getTrimmedTimeRange();
                if (trimWith != null && !element.hasFailed()) {
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
