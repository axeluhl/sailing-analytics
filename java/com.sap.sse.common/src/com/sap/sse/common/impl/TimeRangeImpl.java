package com.sap.sse.common.impl;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class TimeRangeImpl extends Util.Pair<TimePoint, TimePoint> implements TimeRange {
    private static final long serialVersionUID = 8710198176227507300L;
    
    public static TimeRange create(long fromMillis, long toMillis) {
        return new TimeRangeImpl(new MillisecondsTimePoint(fromMillis), new MillisecondsTimePoint(toMillis));
    }

    public TimeRangeImpl(TimePoint from, TimePoint to) {
        super(from == null ? TimePoint.BeginningOfTime : from, to == null ? TimePoint.EndOfTime : to);
        if (from().after(to())) {
            throw new IllegalArgumentException("from " + from() + " must lie before to " + to() + " in a TimeRange");
        }
    }

    @Override
    public int compareTo(TimeRange other) {
        final int result;
        if (other.from().equals(from()) && other.to().equals(to())) {
            result = 0;
        } else {
            result = startsBefore(other) ? -1 : 1;
        }
        return result;
    }

    @Override
    public TimePoint from() {
        return getA();
    }

    @Override
    public TimePoint to() {
        return getB();
    }

    @Override
    public boolean liesWithin(TimeRange other) {
        return from().compareTo(other.from()) >= 0 && to().compareTo(other.to()) <= 0;
    }

    @Override
    public boolean includes(TimeRange other) {
        return other.liesWithin(this);
    }

    @Override
    public boolean intersects(TimeRange other) {
        return includes(other.from()) || includes(other.to()) || liesWithin(other);
    }

    @Override
    public boolean includes(TimePoint timePoint) {
        return from().compareTo(timePoint) <= 0 && to().compareTo(timePoint) >= 0;
    }

    @Override
    public boolean startsBefore(TimeRange other) {
        return from().before(other.from());
    }

    @Override
    public boolean endsAfter(TimeRange other) {
        return to().after(other.to());
    }

    @Override
    public Duration timeDifference(TimePoint timePoint) {
        final Duration result;
        if (includes(timePoint)) {
            result = Duration.NULL;
        } else if (timePoint.before(from())) {
            result = timePoint.until(from());
        } else {
            result = to().until(timePoint);
        }
        return result;
    }

    @Override
    public TimeRange union(TimeRange other) {
        final TimeRange result;
        if (!intersects(other)) {
            result = null;
        } else {
            TimePoint newFrom = startsBefore(other) ? from() : other.from();
            TimePoint newTo = endsAfter(other) ? to() : other.to();
            result = new TimeRangeImpl(newFrom, newTo);
        }
        return result;
    }

    @Override
    public boolean hasOpenBeginning() {
        return from().equals(TimePoint.BeginningOfTime);
    }

    @Override
    public boolean hasOpenEnd() {
        return to().equals(TimePoint.EndOfTime);
    }

    @Override
    public TimeRange intersection(TimeRange other) {
        final TimeRange result;
        if (!intersects(other)) {
            result = null;
        } else {
            TimePoint newFrom = startsBefore(other) ? other.from() : from();
            TimePoint newTo = endsAfter(other) ? other.to() : to();
            result = new TimeRangeImpl(newFrom, newTo);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        final boolean result;
        if (this == obj) {
            result = true;
        } else {
            if (obj instanceof TimeRange) {
                TimeRange other = (TimeRange) obj;
                return from().equals(other.from()) && to().equals(other.to());
            }
            result = super.equals(obj);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return from() + "-" + to();
    }
}
