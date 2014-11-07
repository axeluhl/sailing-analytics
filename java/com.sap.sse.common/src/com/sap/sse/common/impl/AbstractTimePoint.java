package com.sap.sse.common.impl;

import java.util.Comparator;
import java.util.Date;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public abstract class AbstractTimePoint implements TimePoint {
    private static final long serialVersionUID = 8825508619301420378L;

    public static Comparator<TimePoint> TIMEPOINT_COMPARATOR = new SerializableComparator<TimePoint>() {
        private static final long serialVersionUID = 7644726881387366025L;

        @Override
        public int compare(TimePoint o1, TimePoint o2) {
            long o1Millis = o1.asMillis();
            long o2Millis = o2.asMillis();
            return o1Millis<o2Millis?-1:o1Millis==o2Millis?0:1;
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
        final TimePoint result;
        if (duration != null) {
            result = plus(duration.asMillis());
        } else {
            result = this;
        }
        return result;
    }

    @Override
    public TimePoint minus(Duration duration) {
        final TimePoint result;
        if (duration != null) {
            result = minus(duration.asMillis());
        } else {
            result = this;
        }
        return result;
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
        if (this == obj) {
            return true;
        } else if (obj instanceof TimePoint) {
            return compareTo((TimePoint) obj) == 0;
        } else {
            return false;
        }
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
