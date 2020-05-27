package com.sap.sse.gwt.client.async;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MultiTimeRangeImpl;

public class TimeRangeActionsExecutor<Result> {
    public interface TimeRangeAsyncAction<Result> extends AsyncAction<Result> {
        TimeRange getTimeRange();
        void setTimeRange(TimeRange timeRange);
    }

    public interface TimeRangeResultJoiner<T> {
        /**
         * 
         * @param resultsToJoin
         * @param timeRange
         * @return {@code null} to indicate missing data.
         */
        T join(List<Pair<TimeRange, T>> resultsToJoin, TimeRange timeRange);
    }

    private class Request<T> implements AsyncCallback<T> {
        private final TimeRangeAsyncAction<T> action;
        private final AsyncCallback<T> callback;
        private T result = null;

        public Request(TimeRangeAsyncAction<T> asyncAction, AsyncCallback<T> asyncCallback) {
            this.action = asyncAction;
            this.callback = asyncCallback;
        }

        @Override
        public void onSuccess(T result) {
            TimeRange requestedTimeRange = TimeRangeActionsExecutor.this.requestedRangeMap.get(this);
            // If this requests TimeRange was trimmed or the results are already present
            if (requestedTimeRange == null || !requestedTimeRange.equals(action.getTimeRange())) {
                //TODO reduce amount of elements that the joiner has to search through
                result = (T) TimeRangeActionsExecutor.this.joinResults((TimeRangeActionsExecutor<Result>.Request<Result>) this); //TODO Type check
            }
            TimeRangeActionsExecutor.this.requestedRangeMap.remove(this);
            if (requestedTimeRange != null) {
                action.setTimeRange(requestedTimeRange); //TODO Resetting this to what it originally was is inefficient
            }
            if (result != null) {
                this.result = result;
                TimeRangeActionsExecutor.this.receivedRequestsRingBuffer.add((TimeRangeActionsExecutor<Result>.Request<Result>) this); //TODO Type check
                TimeRangeActionsExecutor.this.receivedRange = null;
            }
            if (callback != null) {
                callback.onSuccess(result);
            }
        }

        @Override
        public void onFailure(Throwable caught) {
            TimeRangeActionsExecutor.this.requestedRangeMap.remove(this);
            //TODO Check if other requests are now missing parts of their request
            if (callback != null) {
                callback.onFailure(caught);
            }
        }

        public TimeRangeAsyncAction<T> getAction() {
            return action;
        }

        public T getResult() {
            return result;
        }
    }


    protected final AsyncActionsExecutor executor;
    protected String actionCategory = MarkedAsyncCallback.CATEGORY_GLOBAL;
    protected final TimeRangeResultJoiner<Result> resultJoiner;
    protected final Deque<Request<Result>> waitingForOtherRequestsQueue = new ArrayDeque<>();
    /**
     * Holds {@link TimeRange}s of requests that have been given to the {@link #executor}.
     */
    protected final Map<Request<Result>, TimeRange> requestedRangeMap = new HashMap<>();
    /**
     * Ring buffer holding the last successful responses.
     */
    protected final Deque<Request<Result>> receivedRequestsRingBuffer = new ArrayDeque<Request<Result>>() {
        private static final long serialVersionUID = 1L;
        private final int bufferSize = 16;
        @Override
        public void addFirst(Request<Result> e) {
            while (size() >= bufferSize) {
                super.pollFirst();
            }
            super.addFirst(e);
        }
        @Override
        public void addLast(Request<Result> e) {
            while (size() >= bufferSize) {
                super.pollFirst();
            }
            super.addLast(e);
        }
    };
    /**
     * Get with {@link #getReceivedTimeRange()} as this will be set to {@code null} once it becomes invalid.
     * {@link MultiTimeRange} containing {@link #receivedRequestsRingBuffer} element's {@link TimeRange}s.
     */
    protected MultiTimeRange receivedRange;

    public TimeRangeActionsExecutor(AsyncActionsExecutor actionsExecutor, TimeRangeResultJoiner<Result> resultJoiner) {
        this.executor = actionsExecutor;
        this.resultJoiner = resultJoiner;
    }

    public void execute(TimeRangeAsyncAction<Result> action, AsyncCallback<Result> callback) {
        execute(action, callback, /* forceTimeRange */ false);
    }

    public void execute(TimeRangeAsyncAction<Result> action, AsyncCallback<Result> callback,
            boolean forceTimeRange) {
        Request<Result> request = new Request<>(action, callback);
        TimeRange requestedTimeRange = action.getTimeRange();
        if (!forceTimeRange) {
            TimeRange missingTimeRange = trimActionTimeRange(requestedTimeRange);
            // If all data is already present simply return the results
            if (missingTimeRange == null) {
                request.onSuccess(null);
                return;
            }
            action.setTimeRange(missingTimeRange);
        }
        
        requestedRangeMap.put(request, requestedTimeRange);
        //TODO waitingQueue
        //executor.getNumberOfPendingActionsPerType(actionCategory);
        
        executor.execute(action, actionCategory, request);
    }

    /**
     * @return trimmed {@link TimeRange} or {@code null} if the request is unnecessary since the results are already here.
     */
    private TimeRange trimActionTimeRange(TimeRange timeRange) {
        // Signal to return result immediately if everything is already there
        if (getReceivedTimeRange().includes(timeRange)) {
            return null;
        }
        // Trim against already fetched results and mark them
        for (TimeRange rangeToTrimBy : getReceivedTimeRange()) {
            timeRange = trimTimeRange(timeRange, rangeToTrimBy); //TODO Mark needed results so they don't get dropped
        }
        // Trim against requests already in transit
        for (TimeRange rangeToTrimBy : requestedRangeMap.values()) {
            timeRange = trimTimeRange(timeRange, rangeToTrimBy);
        }
        return timeRange;
    }

    private static TimeRange trimTimeRange(TimeRange toTrim, TimeRange trimWith) {
        //TODO A TimeRange which had lain within toTrim on the first pass might now be usable on a second pass
        if (toTrim.intersects(trimWith) && !toTrim.liesWithin(trimWith) && !toTrim.includes(trimWith)) {
            return toTrim.subtract(trimWith).iterator().next();
        }
        return toTrim;
    }

    private Result joinResults(Request<Result> partialResult) {
        final TimeRange requestedTimeRange = requestedRangeMap.get(partialResult);
        if (requestedTimeRange == null) { //TODO Maybe only log since we could recover by using the trimmed TimeRange from Request.getAction().getTimeRange()? Also this case SHOULD never occur
            throw new IllegalStateException("TimeRange for Request not found in requestedRangeMap");
        }
        List<Pair<TimeRange, Result>> results = receivedRequestsRingBuffer.stream()
                .filter(r -> r.getAction().getTimeRange().intersects(requestedTimeRange))
                .map(r -> new Pair<>(r.getAction().getTimeRange(), r.getResult()))
                .collect(Collectors.toList());
        Result result = resultJoiner.join(results, requestedTimeRange);
        if (result == null) {
            //TODO Data missing! Fire off another request or wait for it to finish?
        }
        return result;
    }

    protected MultiTimeRange getReceivedTimeRange() {
        if (receivedRange == null) {
            TimeRange[] timeRanges = receivedRequestsRingBuffer.stream()
                    .map(Request::getAction)
                    .map(TimeRangeAsyncAction::getTimeRange)
                    .toArray(TimeRange[]::new);
            receivedRange = new MultiTimeRangeImpl(timeRanges);
        }
        return receivedRange;
    }
}
