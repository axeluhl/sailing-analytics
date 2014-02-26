package com.sap.sailing.domain.common.impl;

import java.util.Comparator;
import java.util.Date;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.TimePoint;

public abstract class AbstractTimePoint implements TimePoint {
    private static final long serialVersionUID = 8825508619301420378L;

    public static Comparator<TimePoint> TIMEPOINT_COMPARATOR = new SerializableComparator<TimePoint>() {
        private static final long serialVersionUID = 7644726881387366025L;

        @Override
        public int compare(TimePoint o1, TimePoint o2) {
        	return new Long(o1.asMillis()).compareTo(o2.asMillis());
        }
    };

    @Override
    public int compareTo(TimePoint o) {
        return TIMEPOINT_COMPARATOR.compare(this, o);
    }
    
    @Override
    public Date asDate() {
        Date result = getDateFromCache();
        if (result == null) {
            result = new Date(asMillis());
            cacheDate(result);
        }
        return result;
    }
    
    @Override
    public TimePoint plus(Duration duration) {
        return plus(duration.asMillis());
    }

    @Override
    public TimePoint minus(Duration duration) {
        return minus(duration.asMillis());
    }

    @Override
    public Duration until(TimePoint later) {
        return new MillisecondsDurationImpl(later.asMillis()-asMillis());
    }

    @Override
    public TimePoint plus(long milliseconds) {
        return new MillisecondsTimePoint(asMillis()+milliseconds);
    }
    
    @Override
    public TimePoint minus(long milliseconds) {
        return plus(-milliseconds);
    }

    @Override
    public boolean after(TimePoint other) {
        return this.compareTo(other) > 0;
    }

    @Override
    public boolean before(TimePoint other) {
        return this.compareTo(other) < 0;
    }

    @Override
    public int hashCode() {
        return (int) (asMillis() & Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((TimePoint) obj) == 0;
    }

    @Override
    public String toString() {
        return asDate().toString();
    }

    /**
     * A subclass may implement a cache for the {@link Date} representation by overriding this method as well
     * as {@link #cacheDate(Date)}.
     */
    protected Date getDateFromCache() {
        return null;
    }

    /**
     * A subclass may implement a cache for the {@link Date} representation by overriding this method as well
     * as {@link #getDate()}.
     */
    protected void cacheDate(Date date) {}
}
