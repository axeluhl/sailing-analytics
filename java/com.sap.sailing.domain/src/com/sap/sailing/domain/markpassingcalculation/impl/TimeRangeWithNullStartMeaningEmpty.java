package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * A special time range implementation that keeps {@code from} and {@code to}
 * as passed from the constructor and interprets an open start ({@code from} being {@code null})
 * as an empty interval.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TimeRangeWithNullStartMeaningEmpty {
    private final TimeRange timeRangeOrNull;
    private final TimePoint from;
    private final TimePoint to;
    private final boolean toIsInclusive;

    public TimeRangeWithNullStartMeaningEmpty(TimePoint from, TimePoint to, boolean toIsInclusive) {
        timeRangeOrNull = from == null ? null : new TimeRangeImpl(from, to, toIsInclusive);
        this.from = from;
        this.to = to;
        this.toIsInclusive = toIsInclusive;
    }

    public TimeRangeWithNullStartMeaningEmpty(TimePoint from, TimePoint toExclusive) {
        this(from, toExclusive, /* toIsInclusive */ false);
    }

    public TimeRangeWithNullStartMeaningEmpty getWithNewFrom(TimePoint from) {
        final TimeRangeWithNullStartMeaningEmpty result;
        if (Util.equalsWithNull(from, this.from)) {
            result = this;
        } else {
            result = new TimeRangeWithNullStartMeaningEmpty(from, to, toIsInclusive);
        }
        return result;
    }
    
    public TimeRangeWithNullStartMeaningEmpty getWithNewTo(TimePoint to, boolean toIsInclusive) {
        final TimeRangeWithNullStartMeaningEmpty result;
        if (Util.equalsWithNull(to, this.to) && toIsInclusive == this.toIsInclusive) {
            result = this;
        } else {
            result = new TimeRangeWithNullStartMeaningEmpty(this.from, to, toIsInclusive);
        }
        return result;
    }

    public TimeRangeWithNullStartMeaningEmpty getWithNewTo(TimePoint toExclusive) {
        return getWithNewTo(toExclusive, /* toIsInclusive */ false);
    }

    public TimeRange getTimeRangeOrNull() {
        return timeRangeOrNull;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((timeRangeOrNull == null) ? 0 : timeRangeOrNull.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        result = prime * result + (toIsInclusive ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeRangeWithNullStartMeaningEmpty other = (TimeRangeWithNullStartMeaningEmpty) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (timeRangeOrNull == null) {
            if (other.timeRangeOrNull != null)
                return false;
        } else if (!timeRangeOrNull.equals(other.timeRangeOrNull))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        if (toIsInclusive != other.toIsInclusive)
            return false;
        return true;
    }
}
