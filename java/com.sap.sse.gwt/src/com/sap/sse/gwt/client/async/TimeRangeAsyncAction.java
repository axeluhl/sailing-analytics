package com.sap.sse.gwt.client.async;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * An action that will execute a remote procedure.
 * 
 * 
 *
 * @param <Result> Type returned by remote procedure.
 * @param <Key> Type used to index 
 *
 * @author Tim Hessenmüller (D062243)
 */
public interface TimeRangeAsyncAction<Result, Key> {
    /**
     * Starts execution of remote procedure.
     * Might be called multiple times with new timeRanges.
     * @param timeRanges
     * @param callback
     */
    void execute(Collection<Pair<Key, TimeRange>> timeRanges, AsyncCallback<Result> callback);

    Collection<Pair<Key, TimeRange>> getTimeRanges();
}