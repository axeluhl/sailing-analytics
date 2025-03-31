package com.sap.sse.gwt.client.async;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;

/**
 * An action that will asynchronously execute a remote procedure that is parameterized by time ranges keyed by a set of
 * {@code Key}s (e.g., a competitor, or a boat or similar) and that returns a {@code Result}, most likely again keyed by
 * those {@code Key}s somehow. The action specifies by its return value for the {@link #getTimeRanges()} method for
 * which time ranges it would like to know the results. When
 * {@link TimeRangeActionsExecutor#execute(TimeRangeAsyncAction, TimeRangeAsyncCallback) executed} by a
 * {@link TimeRangeActionsExecutor}, that executor can trim the request time ranges based on cached requests, avoiding
 * redundant requests for overlapping time ranges. After the executor has trimmed the requests the executor will call
 * this action's {@link #execute(Map<Key,TimeRange>, AsyncCallback)} method with the trimmed time ranges.
 * <p>
 * 
 * The benefit of using this approach over making a straight call to the remote procedure is that the
 * {@link TimeRangeActionsExecutor} understands, manages and optimizes the overlaps in concurrent requests.
 *
 * @param <Result>
 *            Type returned by remote procedure.
 * @param <Key>
 *            Type used to index
 * @see TimeRangeActionsExecutor
 *
 * @author Tim Hessenmüller (D062243)
 */
public interface TimeRangeAsyncAction<Result, Key> {
    /**
     * Starts execution of remote procedure. Might be called multiple times with new timeRanges.
     *
     * @param timeRanges
     *            {@link TimeRange}s to request from the server. They might differ from the wanted {@link TimeRange}s
     *            returned by {@link #getTimeRanges()} because of cached results.
     * @param callback
     *            {@link AsyncCallback} to give response to.
     */
    void execute(Map<Key, TimeRange> timeRanges, AsyncCallback<Result> callback);

    /**
     * Gets the wanted {@link TimeRange}s per {@link Key}.
     *
     * @return wanted {@link TimeRange} for each wanted {@link Key}.
     */
    Map<Key, TimeRange> getTimeRanges();
}