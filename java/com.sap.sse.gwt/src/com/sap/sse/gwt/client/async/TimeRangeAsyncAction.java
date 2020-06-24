package com.sap.sse.gwt.client.async;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * An action that will asynchronously execute a remote procedure that is parameterized by time ranges and by a
 * {@code Key} (e.g., a competitor, or a boat or similar) and that returns a {@code Result}. Executed by
 * {@link TimeRangeActionsExecutor}. The benefit of using this class over making a straight call to the remote procedure
 * is that the {@link TimeRangeActionsExecutor} understands, manages and optimized the overlaps in concurrent requests.
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
    void execute(Collection<Pair<Key, TimeRange>> timeRanges, AsyncCallback<Result> callback);

    /**
     * Gets the wanted {@link TimeRange}s per {@link Key}.
     *
     * @return wanted {@link TimeRange} for each wanted {@link Key}.
     */
    Collection<Pair<Key, TimeRange>> getTimeRanges();
}