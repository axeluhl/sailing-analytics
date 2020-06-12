package com.sap.sse.gwt.test.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.async.TimeRangeResultCache;

public class TimeRangeResultCacheTest {
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
        TimeRangeResultCache<Void> trimmer = new TimeRangeResultCache<>();
        TimeRange timeRange = new MilliTimeRange(100, 1000);
        assertEquals(timeRange, trimmer.trimAndRegisterRequest(timeRange, false));
        List<Pair<TimeRange, Void>> expectedResult = new ArrayList<>(1);
        expectedResult.add(new Pair<>(timeRange, null));
        assertArrayEquals(expectedResult.toArray(), trimmer.registerAndCollectResult(timeRange, null, null).toArray());
    }

    @Test
    public void testTrimSinglePass() {
        TimeRangeResultCache<Void> trimmer = new TimeRangeResultCache<>();
        final TimeRange requestRange =  new MilliTimeRange(10_000L, 20_000L);
        final TimeRange expectedRange = new MilliTimeRange(15_000L, 18_000L);
        final TimeRange leftRange =     new MilliTimeRange(10_000L, 15_000L);
        final TimeRange rightRange =    new MilliTimeRange(18_000L, 30_000L);
        final TimeRange includedRange = new MilliTimeRange(16_000L, 17_000L);
        final TimeRange outsideRange =  new MilliTimeRange(50_000L, 100_000L);
        trimmer.trimAndRegisterRequest(leftRange, true);
        trimmer.trimAndRegisterRequest(rightRange, true);
        trimmer.trimAndRegisterRequest(includedRange, true);
        trimmer.trimAndRegisterRequest(outsideRange, true);
        assertEquals(expectedRange, new MilliTimeRange(trimmer.trimAndRegisterRequest(requestRange, false)));
    }

    @Test
    public void testTrimMultiplePasses() {
        TimeRangeResultCache<Void> trimmer = new TimeRangeResultCache<>();
        final TimeRange requestRange =          new MilliTimeRange(100_000L, 200_000L);
        final TimeRange expectedRange =         new MilliTimeRange(150_000L, 190_000L);
        final TimeRange leftRange =             new MilliTimeRange( 90_000L, 125_000L);
        final TimeRange leftSecondPassRange =   new MilliTimeRange(120_000L, 150_000L);
        final TimeRange rightRange =            new MilliTimeRange(190_000L, 300_000L);
        final TimeRange outsideRange =          new MilliTimeRange( 50_000L, 100_000L);
        trimmer.trimAndRegisterRequest(leftSecondPassRange, true);
        trimmer.trimAndRegisterRequest(leftRange, true);
        trimmer.trimAndRegisterRequest(rightRange, true);
        trimmer.trimAndRegisterRequest(outsideRange, true);
        assertEquals(expectedRange, new MilliTimeRange(trimmer.trimAndRegisterRequest(requestRange, false)));
    }

    @Test
    public void testGatherResults() {
        TimeRangeResultCache<Integer> trimmer = new TimeRangeResultCache<>();
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
        trimmer.registerAndCollectResult(leftRange, leftInt, null);
        trimmer.registerAndCollectResult(outsideRange, outsideInt, null);
        TimeRange trimmedRange = trimmer.trimAndRegisterRequest(requestedRange, false);
        assertEquals(expectedRange, trimmedRange);
        List<Pair<TimeRange, Integer>> result = trimmer.registerAndCollectResult(trimmedRange, requestedInt, null);
        assertArrayEquals(expectedResult.toArray(), result.toArray());
    }
}
