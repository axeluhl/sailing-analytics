package com.sap.sse.gwt.test.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.async.TimeRangeActionsExecutor;
import com.sap.sse.gwt.client.async.TimeRangeActionsExecutor.TimeRangeAsyncAction;
import com.sap.sse.gwt.client.async.TimeRangeActionsExecutor.TimeRangeResultJoiner;

public class TimeRangeActionsExecutorTest {
    private class MockAsyncActionsExecutor extends AsyncActionsExecutor {
        @Override
        public <T> void execute(AsyncAction<T> action, AsyncCallback<T> callback) {
            execute(action, MarkedAsyncCallback.CATEGORY_GLOBAL, callback);
        }

        @Override
        public <T> void execute(AsyncAction<T> action, String category, AsyncCallback<T> callback) {
            action.execute(callback);
        }
    }

    private class CounterAction implements TimeRangeAsyncAction<Integer> {
        protected TimeRange timeRange;
        protected final boolean doSucceed;
        protected final boolean doFail;
        protected int counter = 0;

        public CounterAction(TimeRange timeRange, boolean doSucceed, boolean doFail) {
            this.timeRange = timeRange;
            this.doSucceed = doSucceed;
            this.doFail = doFail;
        }

        public int getCounter() {
            return counter;
        }

        @Override
        public void execute(AsyncCallback<Integer> callback) {
            counter++;
            if (doSucceed) callback.onSuccess(counter);
            if (doFail) callback.onFailure(new RuntimeException());
        }

        @Override
        public TimeRange getTimeRange() {
            // TODO Auto-generated method stub
            return timeRange;
        }

        @Override
        public void setTimeRange(TimeRange timeRange) {
            this.timeRange = timeRange;
        }
    }

    private class NopAction<T> implements TimeRangeAsyncAction<T> {
        private TimeRange timeRange;

        public NopAction(TimeRange timeRange) {
            this.timeRange = timeRange;
        }
        @Override
        public void execute(AsyncCallback<T> callback) {
            // NOP
        }
        @Override
        public TimeRange getTimeRange() {
            return timeRange;
        }
        @Override
        public void setTimeRange(TimeRange timeRange) {
            this.timeRange = timeRange;
        }
    }

    private static final AsyncCallback<Void> EMPTY_VOID_CALLBACK = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
        }
        @Override
        public void onSuccess(Void result) {
        }
    };
    private static final AsyncCallback<Integer> EMPTY_INTEGER_CALLBACK = new AsyncCallback<Integer>() {
        @Override
        public void onFailure(Throwable caught) {
        }
        @Override
        public void onSuccess(Integer result) {
        }
    };

    @Test
    public void testSimpleRequest() {
        TimeRangeActionsExecutor<Integer> exec = new TimeRangeActionsExecutor<>(new MockAsyncActionsExecutor(), null);
        final long now = System.currentTimeMillis();
        CounterAction action = new CounterAction(new TimeRangeImpl(new MillisecondsTimePoint(now), new MillisecondsTimePoint(now + 1000)), true, true);
        exec.execute(action, EMPTY_INTEGER_CALLBACK);
        assertEquals(1, action.getCounter());
    }

    @Test
    public void testTrimFromResults() {
        TimeRangeActionsExecutor<Integer> exec = new TimeRangeActionsExecutor<Integer>(new MockAsyncActionsExecutor(), new TimeRangeResultJoiner<Integer>() {
            @Override
            public Integer join(List<Pair<TimeRange, Integer>> resultsToJoin, TimeRange timeRange) {
                return 0;
            }
        });
        final TimeRange requestRange = new TimeRangeImpl(new MillisecondsTimePoint(10_000L), new MillisecondsTimePoint(20_000L));
        final TimeRange expectedRange = new TimeRangeImpl(new MillisecondsTimePoint(15_000L), new MillisecondsTimePoint(18_000L));
        final TimeRange leftRange = new TimeRangeImpl(new MillisecondsTimePoint(5_000L), new MillisecondsTimePoint(15_000L));
        final TimeRange rightRange = new TimeRangeImpl(new MillisecondsTimePoint(18_000L), new MillisecondsTimePoint(30_000L));
        final TimeRange includedRange = new TimeRangeImpl(new MillisecondsTimePoint(16_000L), new MillisecondsTimePoint(17_000L));
        final TimeRange outsideRange = new TimeRangeImpl(new MillisecondsTimePoint(50_000L), new MillisecondsTimePoint(100_000L));
        // Prefill with trim ranges
        exec.execute(new CounterAction(leftRange, true, false), EMPTY_INTEGER_CALLBACK);
        exec.execute(new CounterAction(rightRange, true, false), EMPTY_INTEGER_CALLBACK);
        exec.execute(new CounterAction(includedRange, true, false), EMPTY_INTEGER_CALLBACK);
        exec.execute(new CounterAction(outsideRange, true, false), EMPTY_INTEGER_CALLBACK);

        List<TimeRange> timeRangeChanges = new ArrayList<>();
        CounterAction action = new CounterAction(requestRange, true, false) {
            @Override
            public void setTimeRange(TimeRange timeRange) {
                super.setTimeRange(timeRange);
                timeRangeChanges.add(timeRange);
            }
        };
        List<TimeRange> expectedRangeChanges = new ArrayList<>();
        expectedRangeChanges.add(expectedRange);
        expectedRangeChanges.add(requestRange);
        exec.execute(action, EMPTY_INTEGER_CALLBACK);
        assertListEquals(expectedRangeChanges, timeRangeChanges);
    }

    @Test
    public void testTrimFromResultsMultiplePasses() {
        
    }

    @Test
    public void testTrimFromInTransit() {
        TimeRangeActionsExecutor<Integer> exec = new TimeRangeActionsExecutor<>(new MockAsyncActionsExecutor(), new TimeRangeResultJoiner<Integer>() {
            @Override
            public Integer join(List<Pair<TimeRange, Integer>> resultsToJoin, TimeRange timeRange) {
                return 0;
            }
        });
        final TimeRange requestRange = new TimeRangeImpl(new MillisecondsTimePoint(200_000L), new MillisecondsTimePoint(300_000L));
        final TimeRange expectedRange = new TimeRangeImpl(new MillisecondsTimePoint(210_000L), new MillisecondsTimePoint(250_000L));
        final TimeRange leftRange = new TimeRangeImpl(new MillisecondsTimePoint(100_000L), new MillisecondsTimePoint(210_000L));
        final TimeRange rightRange = new TimeRangeImpl(new MillisecondsTimePoint(250_000L), new MillisecondsTimePoint(400_000L));
        final TimeRange includedRange = new TimeRangeImpl(new MillisecondsTimePoint(220_000L), new MillisecondsTimePoint(245_000L));
        final TimeRange outsideRange = new TimeRangeImpl(new MillisecondsTimePoint(50_000L), new MillisecondsTimePoint(100_000L));
        // Put actions into transit
        exec.execute(new NopAction<>(leftRange), EMPTY_INTEGER_CALLBACK);
        exec.execute(new NopAction<>(rightRange), EMPTY_INTEGER_CALLBACK);
        exec.execute(new NopAction<>(includedRange), EMPTY_INTEGER_CALLBACK);
        exec.execute(new NopAction<>(outsideRange), EMPTY_INTEGER_CALLBACK);

        List<TimeRange> timeRangeChanges = new ArrayList<>();
        CounterAction action = new CounterAction(requestRange, true, false) {
            @Override
            public void setTimeRange(TimeRange timeRange) {
                super.setTimeRange(timeRange);
                timeRangeChanges.add(timeRange);
            }
        };
        List<TimeRange> expectedRangeChanges = new ArrayList<>();
        expectedRangeChanges.add(expectedRange);
        expectedRangeChanges.add(requestRange);
        exec.execute(action, EMPTY_INTEGER_CALLBACK);
        assertListEquals(expectedRangeChanges, timeRangeChanges);
    }

    private <T> void assertListEquals(List<T> expected, List<T> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        Iterator<T> expectedIter = expected.iterator();
        Iterator<T> actualIter = actual.iterator();
        while (expectedIter.hasNext()) {
            assertEquals(expectedIter.next(), actualIter.next());
        }
    }
}
