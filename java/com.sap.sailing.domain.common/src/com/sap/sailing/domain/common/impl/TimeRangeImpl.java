package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class TimeRangeImpl extends Pair<TimePoint, TimePoint> implements TimeRange {
    private static final long serialVersionUID = 8710198176227507300L;

    public TimeRangeImpl(TimePoint from, TimePoint to) {
        super(from, to);
        if (from.after(to)) throw new IllegalArgumentException(String.format("from (%s) must lie before to (%s) in a TimeRange", from, to));
    }

    @Override
    public int compareTo(TimeRange arg0) {
        // TODO Auto-generated method stub
        return 0;
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
        return from().compareTo(other.from()) >= 0 && to().compareTo(other.to()) <=0;
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
    public long timeDifference(TimePoint timePoint) {
        if (includes(timePoint)) return 0;
        if (timePoint.before(from())) return from().asMillis() - timePoint.asMillis();
        return to().asMillis() - timePoint.asMillis();
    }
}
