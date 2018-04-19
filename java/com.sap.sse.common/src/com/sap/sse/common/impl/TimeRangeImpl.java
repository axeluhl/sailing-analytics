package com.sap.sse.common.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Duration;
import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class TimeRangeImpl extends Util.Pair<TimePoint, TimePoint> implements TimeRange {
    private static final long serialVersionUID = 8710198176227507300L;

    // required for some serialization frameworks such as GWT RPC
    @Deprecated
    TimeRangeImpl() {
    }
    
    public static TimeRange create(long fromMillis, long toMillisExclusive) {
        return new TimeRangeImpl(new MillisecondsTimePoint(fromMillis), new MillisecondsTimePoint(toMillisExclusive));
    }

    public TimeRangeImpl(TimePoint from, TimePoint to, boolean toIsInclusive) {
        this(from, computeInclusiveOrExclusiveTo(to, toIsInclusive));
    }

    private static TimePoint computeInclusiveOrExclusiveTo(TimePoint to, boolean toIsInclusive) {
        final TimePoint finalTo;
        if (toIsInclusive) {
            if (to == null) {
                finalTo = null;
            } else {
                finalTo = to.plus(1); // add the smallest increment possible with the current time point representation
            }
        } else {
            finalTo = to;
        }
        return finalTo;
    }
    /**
     * @param from
     *            if {@code null}, the time range is considered open on its "left" end, and all {@link TimePoint}s at or
     *            after {@link TimePoint#BeginningOfTime} and at or before {@code to} are considered {@link #includes
     *            included} in this time range.
     * @param toExclusive
     *            if {@code null}, the time range is considered open on its "right" end, and all {@link TimePoint}s at
     *            or before {@link TimePoint#EndOfTime} and at or after {@code from} are considered {@link #includes
     *            included} in this time range.
     */
    public TimeRangeImpl(TimePoint from, TimePoint toExclusive) {
        super(from == null ? TimePoint.BeginningOfTime : from, toExclusive == null ? TimePoint.EndOfTime : toExclusive);
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
            if (from().equals(other.from())) {
                result = to().compareTo(other.to());
            } else {
                result = from().compareTo(other.from());
            }
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
    public boolean isEmpty() {
        return from().equals(to());
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
    public boolean touches(TimeRange other) {
        return intersects(other) || from().equals(other.to()) || to().equals(other.from());
    }

    @Override
    public boolean includes(TimePoint timePoint) {
        return from().compareTo(timePoint) <= 0 && to().compareTo(timePoint) > 0;
    }

    @Override
    public boolean startsBefore(TimeRange other) {
        return from().before(other.from());
    }

    @Override
    public boolean startsAtOrAfter(TimePoint timePoint) {
        return !from().before(timePoint);
    }

    @Override
    public boolean startsAfter(TimeRange other) {
        return startsAtOrAfter(other.to());
    }

    @Override
    public boolean endsAfter(TimeRange other) {
        return to().after(other.to());
    }

    @Override
    public boolean endsBefore(TimePoint timePoint) {
        return !to().after(timePoint);
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
        if (!touches(other)) {
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
    public MultiTimeRange subtract(TimeRange other) {
        final List<TimeRange> result = new ArrayList<>();
        if (this.startsBefore(other)) {
            final TimePoint otherFrom = other.from();
            final TimePoint thisTo = this.to();
            final TimePoint to = otherFrom.after(thisTo) ? thisTo : otherFrom;
            final TimeRange remainder = new TimeRangeImpl(this.from(), to);
            result.add(remainder);
        }
        if (this.endsAfter(other)) {
            final TimePoint otherTo = other.to();
            final TimePoint thisFrom = this.from();
            final TimePoint from = otherTo.after(thisFrom) ? otherTo : thisFrom;
            final TimeRange remainder = new TimeRangeImpl(from, this.to());
            result.add(remainder);
        }
        return new MultiTimeRangeImpl(result);
    }
    
    @Override
    public Duration getDuration() {
        return from().until(to());
    }

    @Override
    public String toString() {
        return from() + "-" + to();
    }
}
