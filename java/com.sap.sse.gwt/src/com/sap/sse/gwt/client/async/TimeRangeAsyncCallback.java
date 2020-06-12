package com.sap.sse.gwt.client.async;

import java.util.List;
import java.util.Map;

import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

public interface TimeRangeAsyncCallback<Result, SubResult, Key> {
    Map<Key, SubResult> unzipResults(Result result);
    SubResult joinSubResults(TimeRange timeRange, List<Pair<TimeRange, SubResult>> toJoin);

    void onSuccess(Map<Key, Pair<TimeRange, SubResult>> results);
    void onFailure(Throwable caught);
}