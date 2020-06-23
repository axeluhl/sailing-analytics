package com.sap.sse.gwt.test.client;

import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

class MilliTimeRange extends TimeRangeImpl {
    private static final long serialVersionUID = -3274328893091285564L;

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