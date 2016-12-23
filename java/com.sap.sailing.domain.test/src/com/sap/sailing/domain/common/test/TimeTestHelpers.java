package com.sap.sailing.domain.common.test;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class TimeTestHelpers {

    protected static TimeRange create(Long from, Long to) {
        return new TimeRangeImpl(from==null?null:new MillisecondsTimePoint(from), to==null?null:new MillisecondsTimePoint(to));
    }

    protected static TimeRange create(int from, int to) {
        return create((long) from, (long) to);
    }

    protected static TimeRange create(int from, long to) {
        return create((long) from, to);
    }

    protected static TimeRange create(long from, int to) {
        return create(from, (long) to);
    }

    protected static TimePoint create(long millis) {
        return new MillisecondsTimePoint(millis);
    }

}
