package com.sap.sse.gwt.client.async;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

public class TimeRangeResultCacheTest {
    private static AsyncCallback<Void> getCallback(boolean assertSuccess) {
        return new AsyncCallback<Void>() {
            private boolean triggered = false;
            @Override
            public void onFailure(Throwable caught) {
                assertTriggeredOnce();
                if (assertSuccess) {
                    assertTrue("Expected onSuccess but got onFailure", false); // Fail test
                }
            }
            @Override
            public void onSuccess(Void result) {
                assertTriggeredOnce();
                if (!assertSuccess) {
                    assertTrue("Expected onFailure but got onSuccess", false); // Fail test
                }
            }
            private void assertTriggeredOnce() {
                assertFalse("Callback was triggered more than once", triggered);
                triggered = true;
            }
        };
    }
    private static <R, K> TimeRangeAsyncAction<R, K> getDummyAction() {
        return new TimeRangeAsyncAction<R, K>() {
            @Override
            public void execute(Collection<Pair<K, TimeRange>> timeRanges, AsyncCallback<R> callback) {
                // Nop
            }
            @Override
            public Collection<Pair<K, TimeRange>> getTimeRanges() {
                return Collections.emptyList();
            }
        };
    }

    @Test
    public void testSimpleRequest() {
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        TimeRange timeRange = new MilliTimeRange(100, 1000);
        TimeRangeAsyncAction<Void, Integer> action = getDummyAction();
        final AtomicBoolean callbackHasRun = new AtomicBoolean(false);
        assertEquals(0, cache.getCacheSize());
        assertEquals(timeRange, cache.trimAndRegisterRequest(timeRange, /* force */ false, action, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                assertTrue(false);
            }
            @Override
            public void onSuccess(Void result) {
                List<Pair<TimeRange, Void>> expectedResult = new ArrayList<>(1);
                expectedResult.add(new Pair<>(timeRange, null));
                assertArrayEquals(expectedResult.toArray(), cache.getResults(action).toArray());
                assertFalse(callbackHasRun.getAndSet(true)); // Fails if run more than once
            }
        }));
        assertEquals(1, cache.getCacheSize());
        cache.registerResult(action, null);
        assertEquals(0, cache.getCacheSize()); // Request gets removed by cache.getResults(action) in callback
        assertTrue(callbackHasRun.get());
    }

    @Test
    public void testDependentRequests() {
        /* Request graph:

            first (completes)
              |  \
              |   second (fails)
              |  /
            third (successful but does not complete action because of second)
              |
            fourth (completes action since third is successful)
        */
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        final TimeRangeAsyncAction<Void, Integer> firstAction = getDummyAction();
        final TimeRangeAsyncAction<Void, Integer> secondAction = getDummyAction();
        final TimeRangeAsyncAction<Void, Integer> thirdAction = getDummyAction();
        final TimeRangeAsyncAction<Void, Integer> fourthAction = getDummyAction();
        final TimeRange firstRange =            new MilliTimeRange(10_000L, 20_000L);
        final TimeRange firstExpectedRange =    new MilliTimeRange(10_000L, 20_000L);
        final TimeRange secondRange =           new MilliTimeRange(11_000L, 21_000L);
        final TimeRange secondExpectedRange =   new MilliTimeRange(20_000L, 21_000L);
        final TimeRange thirdRange =            new MilliTimeRange(12_000L, 22_000L);
        final TimeRange thirdExpectedRange =    new MilliTimeRange(21_000L, 22_000L);
        final TimeRange fourthRange =           new MilliTimeRange(21_000L, 23_000L);
        final TimeRange fourthExpectedRange =   new MilliTimeRange(22_000L, 23_000L);
        assertEquals(0, cache.getCacheSize());
        assertEquals(firstExpectedRange, cache.trimAndRegisterRequest(firstRange, false, firstAction, getCallback(true)));
        assertEquals(1, cache.getCacheSize());
        assertEquals(secondExpectedRange, cache.trimAndRegisterRequest(secondRange, false, secondAction, getCallback(false)));
        assertEquals(2, cache.getCacheSize());
        assertEquals(thirdExpectedRange, cache.trimAndRegisterRequest(thirdRange, false, thirdAction, getCallback(false)));
        assertEquals(3, cache.getCacheSize());
        assertEquals(fourthExpectedRange, cache.trimAndRegisterRequest(fourthRange, false, fourthAction, getCallback(true)));
        assertEquals(4, cache.getCacheSize());
        cache.registerResult(firstAction, null);
        assertEquals(4, cache.getCacheSize());
        cache.registerFailure(secondAction, new Throwable("Second request failed"));
        assertEquals(2, cache.getCacheSize());
        cache.registerResult(fourthAction, null);
        assertEquals(2, cache.getCacheSize());
        cache.registerResult(thirdAction, null);
        assertEquals(2, cache.getCacheSize());
        cache.getResults(fourthAction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterResultForNonExistentRequest() {
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        cache.registerResult(getDummyAction(), null);
    }

    @Test
    public void testTrimSinglePass() {
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        final TimeRange requestRange =  new MilliTimeRange(10_000L, 20_000L);
        final TimeRange expectedRange = new MilliTimeRange(15_000L, 18_000L);
        final TimeRange leftRange =     new MilliTimeRange(10_000L, 15_000L);
        final TimeRange rightRange =    new MilliTimeRange(18_000L, 30_000L);
        final TimeRange includedRange = new MilliTimeRange(16_000L, 17_000L);
        final TimeRange outsideRange =  new MilliTimeRange(50_000L, 100_000L);
        cache.trimAndRegisterRequest(leftRange, true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(rightRange, true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(includedRange, true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(outsideRange, true, getDummyAction(), getCallback(true));
        assertEquals(expectedRange, new MilliTimeRange(cache.trimAndRegisterRequest(requestRange, /* force */ false, getDummyAction(), getCallback(true))));
    }

    @Test
    public void testTrimMultiplePasses() {
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        final TimeRange requestRange =          new MilliTimeRange(100_000L, 200_000L);
        final TimeRange expectedRange =         new MilliTimeRange(150_000L, 190_000L);
        final TimeRange leftRange =             new MilliTimeRange( 90_000L, 125_000L);
        final TimeRange leftSecondPassRange =   new MilliTimeRange(120_000L, 150_000L);
        final TimeRange rightRange =            new MilliTimeRange(190_000L, 300_000L);
        final TimeRange outsideRange =          new MilliTimeRange( 50_000L, 100_000L);
        cache.trimAndRegisterRequest(leftSecondPassRange, /* force */ true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(leftRange, /* force */ true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(rightRange, /* force */ true, getDummyAction(), getCallback(true));
        cache.trimAndRegisterRequest(outsideRange, /* force */ true, getDummyAction(), getCallback(true));
        assertEquals(expectedRange, new MilliTimeRange(cache.trimAndRegisterRequest(requestRange, /* force */ false, getDummyAction(), getCallback(true))));
    }

    @Test
    public void testTrimRequestElimination() {
        TimeRangeResultCache<Void> cache = new TimeRangeResultCache<>();
        final TimeRangeAsyncAction<Void, Integer> requestAction = getDummyAction();
        final TimeRangeAsyncAction<Void, Integer> surroundingAction = getDummyAction();
        final TimeRange requestRange =          new MilliTimeRange(150_000L, 250_000L);
        final TimeRange surroundingRange =      new MilliTimeRange(100_000L, 300_000L);
        assertEquals(surroundingRange, cache.trimAndRegisterRequest(surroundingRange, true, surroundingAction, getCallback(true)));
        assertEquals(null, cache.trimAndRegisterRequest(requestRange, false, requestAction, getCallback(true)));
        assertEquals(2, cache.getCacheSize());
        cache.registerResult(surroundingAction, null);
        cache.getResults(surroundingAction);
        assertEquals(2, cache.getCacheSize());
        cache.registerResult(requestAction, null);
        cache.getResults(requestAction);
        assertEquals(0, cache.getCacheSize());
    }

    @Test
    public void testGatherResults() {
        TimeRangeResultCache<Integer> cache = new TimeRangeResultCache<>();
        final TimeRangeAsyncAction<Integer, Integer> requestAction = getDummyAction();
        final TimeRangeAsyncAction<Integer, Integer> leftAction = getDummyAction();
        final TimeRangeAsyncAction<Integer, Integer> outsideAction = getDummyAction();
        final Integer requestedInt = 1;
        final Integer leftInt = -1;
        final Integer outsideInt = 99;
        final TimeRange requestedRange =        new MilliTimeRange(100_000L, 200_000L);
        final TimeRange expectedRange =         new MilliTimeRange(150_000L, 200_000L);
        final TimeRange leftRange =             new MilliTimeRange( 50_000L, 150_000L);
        final TimeRange outsideRange =          new MilliTimeRange(200_000L, 300_000L);
        final List<Pair<TimeRange, Integer>> expectedResult = new ArrayList<>();
        expectedResult.add(new Pair<>(expectedRange, requestedInt));
        expectedResult.add(new Pair<>(leftRange, leftInt));
        final AtomicBoolean callbackHasRun = new AtomicBoolean(false);
        assertEquals(leftRange, cache.trimAndRegisterRequest(leftRange, true, leftAction, getCallback(true)));
        assertEquals(outsideRange, cache.trimAndRegisterRequest(outsideRange, true, outsideAction, getCallback(true)));
        TimeRange trimmedRange = cache.trimAndRegisterRequest(requestedRange, /* force */ false, requestAction, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertArrayEquals(expectedResult.toArray(), cache.getResults(requestAction).toArray());
                assertFalse(callbackHasRun.getAndSet(true));
            }
            @Override
            public void onFailure(Throwable caught) {
                assertTrue(false);
            }
        });
        assertEquals(expectedRange, trimmedRange);
        cache.registerResult(leftAction, leftInt);
        cache.registerResult(outsideAction, outsideInt);
        cache.registerResult(requestAction, requestedInt);
        assertTrue(callbackHasRun.get());
    }
}
