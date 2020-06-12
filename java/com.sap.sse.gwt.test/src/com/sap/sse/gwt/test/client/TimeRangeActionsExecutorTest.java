package com.sap.sse.gwt.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.TimeRangeActionsExecutor;
import com.sap.sse.gwt.client.async.TimeRangeAsyncAction;
import com.sap.sse.gwt.client.async.TimeRangeAsyncCallback;

public class TimeRangeActionsExecutorTest {
    private class MockAsyncActionsExecutor extends AsyncActionsExecutor {
        @Override
        public <T> void execute(AsyncAction<T> action, String category, AsyncCallback<T> callback) {
            action.execute(callback);
        }
    }

    private class MilliTimeRange extends TimeRangeImpl {
        public MilliTimeRange(long from, long to) {
            super(new MillisecondsTimePoint(from), new MillisecondsTimePoint(to));
        }

        public MilliTimeRange(TimeRange timeRange) {
            super(timeRange.from(), timeRange.to());
        }

        @Override
        public String toString() {
            return from().asMillis() + " - " + to().asMillis();
        }
    }

    @Test
    public void testSimpleRequest() {
        TimeRangeActionsExecutor<Map<String, Integer>, Integer, String> exec = new TimeRangeActionsExecutor<>(new MockAsyncActionsExecutor());
        final AtomicBoolean actionHasRun = new AtomicBoolean(false);
        final AtomicBoolean callbackHasRun = new AtomicBoolean(false);
        exec.execute(new TimeRangeAsyncAction<Map<String,Integer>, String>() {
            @Override
            public void execute(Collection<Pair<String, TimeRange>> timeRanges,
                    AsyncCallback<Map<String, Integer>> callback) {
                actionHasRun.set(true);
                Map<String, Integer> results = new HashMap<>();
                for (Pair<String, TimeRange> channel : timeRanges) {
                    results.put(channel.getA(), channel.getA().length());
                }
                callback.onSuccess(results);
            }
            @Override
            public Collection<Pair<String, TimeRange>> getTimeRanges() {
                Collection<Pair<String, TimeRange>> channels = new ArrayList<>();
                channels.add(new Pair<>("a", new MilliTimeRange(0, 1)));
                channels.add(new Pair<>("bb", new MilliTimeRange(1, 2)));
                return channels;
            }
        }, new TimeRangeAsyncCallback<Map<String,Integer>, Integer, String>() {
            @Override
            public Map<String, Integer> unzipResults(Map<String, Integer> result) {
                return result;
            }
            @Override
            public void onSuccess(Map<String, Pair<TimeRange, Integer>> results) {
                callbackHasRun.set(true);
                assertEquals(2, results.size());
                for (Map.Entry<String, Pair<TimeRange, Integer>> result : results.entrySet()) {
                    assertEquals(result.getKey().length(), result.getValue().getB().intValue());
                }
            }
            @Override
            public void onFailure(Throwable caught) {
                assertTrue(false);
            }
            @Override
            public Integer joinSubResults(TimeRange timeRange, List<Pair<TimeRange, Integer>> toJoin) {
                return toJoin.get(0).getB();
            }
        });
        assertTrue(actionHasRun.get());
        assertTrue(callbackHasRun.get());
    }

    @Test
    public void testOutOfOrderDelivery() {
        throw new UnsupportedOperationException(); //TODO Implement
    }
}
