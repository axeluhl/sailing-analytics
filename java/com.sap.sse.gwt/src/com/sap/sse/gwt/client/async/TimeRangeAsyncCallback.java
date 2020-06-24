package com.sap.sse.gwt.client.async;

import java.util.List;
import java.util.Map;

import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * Interface that the caller must implement to receive a response from a remote procedure call initiated by a
 * {@link TimeRangeAsyncAction}.
 * <p>
 *
 * When used with a {@link TimeRangeActionsExecutor} the data flow looks like this:
 * <ol>
 * <li>{@link TimeRangeAsyncAction} gets executed with a compound request for potentially several keys and returns a
 * compound server response of type {@code Result}
 * <li>The response gets split up into {@link SubResult}s by {@link Key} by {@link #unzipResults(Object)}.
 * <li>By {@link Key} the {@link SubResult}s get cached and get put into {@link #joinSubResults(TimeRange, List)} along
 * with all other required cached {@link SubResult}s.</li>
 * <li>The completed {@link SubResult}s returned by {@link #joinSubResults(TimeRange, List)} get collected and put into
 * {@link #onSuccess(Map)}.</li>
 * </ol>
 *
 * @param <Result>
 *            The type of the compound result returned by the compound request for multiple keys
 * @param <SubResult>
 *            The type of a result for a single key and time range
 * @param <Key>
 *            The type of key, each of which can have a separate time range in the compound request, and for each one a
 *            separate {@code SubResult} is expected to be provided in the compound {@code Result} object.
 * @see TimeRangeAsyncAction
 * @see TimeRangeActionsExecutor
 * @author Tim Hessenmüller (D062243)
 */
public interface TimeRangeAsyncCallback<Result, SubResult, Key> {
    /**
     * Splits up a server response into {@link SubResult}s by {@link Key}.
     *
     * @param result
     *            {@link Result} returned by server.
     * @return {@link Map} of {@link Key} {@link SubResult} pairs.
     */
    Map<Key, SubResult> unzipResults(Result result);

    /**
     * Joins possibly multiple {@link SubResult}s with different {@link TimeRange}s together to form the originally
     * requested {@link SubResult} over {@code timeRange}.
     *
     * @param timeRange
     *            originally requested {@link TimeRange}.
     * @param toJoin
     *            {@link List} of cached {@link SubResult}s with their respective {@link TimeRange}s.
     * @return completed {@link SubResult}.
     */
    SubResult joinSubResults(TimeRange timeRange, List<Pair<TimeRange, SubResult>> toJoin);

    /**
     * Called after successful request and joining of {@link SubResult}s.<br>
     * It might be useful to apply the reverse operation of {@link #unzipResults(Object)} to {@code results}.
     *
     * @param results
     *            result of the remote procedure call.
     */
    void onSuccess(Map<Key, Pair<TimeRange, SubResult>> results);

    /**
     * Called when an exception occurs.
     *
     * @param caught
     *            {@link Throwable}
     */
    void onFailure(Throwable caught);
}