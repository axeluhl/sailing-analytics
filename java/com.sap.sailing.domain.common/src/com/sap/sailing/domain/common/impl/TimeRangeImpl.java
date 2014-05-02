package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class TimeRangeImpl extends Pair<TimePoint, TimePoint> implements TimeRange {
    public static final TimePoint BeginningOfTime = new MillisecondsTimePoint(Long.MIN_VALUE);
    public static final TimePoint EndOfTime = new MillisecondsTimePoint(Long.MAX_VALUE);
    private static final long serialVersionUID = 8710198176227507300L;
    
    public static TimeRange create(long fromMillis, long toMillis) {
        return new TimeRangeImpl(new MillisecondsTimePoint(fromMillis), new MillisecondsTimePoint(toMillis));
    }

    public TimeRangeImpl(TimePoint from, TimePoint to) {
        super(from == null ? BeginningOfTime : from, to == null ? EndOfTime : to);
        if (from().after(to())) throw new IllegalArgumentException(String.format("from (%s) must lie before to (%s) in a TimeRange", from(), to()));
    }

    @Override
    public int compareTo(TimeRange other) {
        if (other.from().equals(from()) && other.to().equals(to())) return 0;
        return startsBefore(other) ? -1 : 1;
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
        if (! intersects(other)) return null;
        TimePoint newFrom = startsBefore(other) ? from() : other.from();
        TimePoint newTo = endsAfter(other) ? to() : other.to();
        return new TimeRangeImpl(newFrom, newTo);
    }

    @Override
    public boolean openBeginning() {
        return from().equals(BeginningOfTime);
    }

    @Override
    public boolean openEnd() {
        return to().equals(EndOfTime);
    }

    @Override
    public TimeRange intersection(TimeRange other) {
        if (! intersects(other)) return null;
        TimePoint newFrom = startsBefore(other) ? other.from() : from();
        TimePoint newTo = endsAfter(other) ? other.to() : to();
        return new TimeRangeImpl(newFrom, newTo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof TimeRange) {
            TimeRange other = (TimeRange) obj;
            return from().equals(other.from()) && to().equals(other.to());
        }
        return super.equals(obj);
    }
    
    @Override
    public String toString() {
        return from() + "-" + to();
    }
}
