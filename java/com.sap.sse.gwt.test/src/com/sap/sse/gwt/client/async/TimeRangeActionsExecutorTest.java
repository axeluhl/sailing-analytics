package com.sap.sse.gwt.client.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.TimeRangeImpl;

public class TimeRangeActionsExecutorTest {
    private class MockAsyncActionsExecutor extends AsyncActionsExecutor {
        @Override
        public <T> void execute(AsyncAction<T> action, String category, AsyncCallback<T> callback) {
            action.execute(callback);
        }
    }

    @Test
    public void testSimpleRequest() {
        TimeRangeActionsExecutor<Map<String, Integer>, Integer, String> exec = new TimeRangeActionsExecutor<>(
                new MockAsyncActionsExecutor());
        final AtomicBoolean actionHasRun = new AtomicBoolean(false);
        final AtomicBoolean callbackHasRun = new AtomicBoolean(false);
        exec.execute(new TimeRangeAsyncAction<Map<String, Integer>, String>() {
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
        }, new TimeRangeAsyncCallback<Map<String, Integer>, Integer, String>() {
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
        TimeRangeActionsExecutor<Map<String, Integer>, Integer, String> exec = new TimeRangeActionsExecutor<>(
                new MockAsyncActionsExecutor());
        final AtomicBoolean callbackHasRun = new AtomicBoolean(false);
        AtomicReference<AsyncCallback<Map<String, Integer>>> lateCallback = new AtomicReference<>();
        // Fire off first request which will simulate a long round trip time
        exec.execute(new TimeRangeAsyncAction<Map<String, Integer>, String>() {
            @Override
            public void execute(Collection<Pair<String, TimeRange>> timeRanges,
                    AsyncCallback<Map<String, Integer>> callback) {
                // Don't call callback now
                lateCallback.set(callback);
            }

            @Override
            public Collection<Pair<String, TimeRange>> getTimeRanges() {
                List<Pair<String, TimeRange>> timeRanges = new ArrayList<>();
                timeRanges.add(new Pair<>("x", TimeRangeImpl.create(100_000L, 120_000L)));
                return timeRanges;
            }
        }, new TimeRangeAsyncCallback<Map<String, Integer>, Integer, String>() {

            @Override
            public Map<String, Integer> unzipResults(Map<String, Integer> result) {
                return result;
            }

            @Override
            public void onSuccess(Map<String, Pair<TimeRange, Integer>> results) {
                assertEquals(Integer.valueOf(1), results.get("x").getB());
            }

            @Override
            public void onFailure(Throwable caught) {
                assertTrue(false); // Fail test
            }

            @Override
            public Integer joinSubResults(TimeRange timeRange, List<Pair<TimeRange, Integer>> toJoin) {
                return toJoin.stream().mapToInt(Pair::getB).sum();
            }
        });
        // Fire off second request which requires the results from the first request but that one is still in transit
        final TimeRange timeRange = TimeRangeImpl.create(110_000L, 130_000L);
        exec.execute(new TimeRangeAsyncAction<Map<String, Integer>, String>() {
            @Override
            public void execute(Collection<Pair<String, TimeRange>> timeRanges,
                    AsyncCallback<Map<String, Integer>> callback) {
                Map<String, Integer> result = new HashMap<>();
                result.put("x", 2);
                callback.onSuccess(result);
            }

            @Override
            public Collection<Pair<String, TimeRange>> getTimeRanges() {
                List<Pair<String, TimeRange>> timeRanges = new ArrayList<>();
                timeRanges.add(new Pair<>("x", timeRange));
                return timeRanges;
            }
        }, new TimeRangeAsyncCallback<Map<String, Integer>, Integer, String>() {

            @Override
            public Map<String, Integer> unzipResults(Map<String, Integer> result) {
                return result;
            }

            @Override
            public void onSuccess(Map<String, Pair<TimeRange, Integer>> results) {
                Pair<TimeRange, Integer> result = results.get("x");
                assertNotNull(result);
                assertEquals(timeRange, result.getA());
                assertEquals(Integer.valueOf(3), result.getB());
                callbackHasRun.set(true);
            }

            @Override
            public void onFailure(Throwable caught) {
                assertTrue(false); // Fail test
            }

            @Override
            public Integer joinSubResults(TimeRange timeRange, List<Pair<TimeRange, Integer>> toJoin) {
                return toJoin.stream().mapToInt(Pair::getB).sum();
            }
        });
        // After the second request has finished the first one now gets answered
        Map<String, Integer> resultsFromFirstRequest = new HashMap<>();
        resultsFromFirstRequest.put("x", 1);
        lateCallback.get().onSuccess(resultsFromFirstRequest);
        assertTrue(callbackHasRun.get());
    }
}
