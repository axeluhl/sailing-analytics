package com.sap.sailing.domain.base.impl;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.util.SerializableComparator;

public abstract class AbstractTimePoint implements TimePoint {
    private static final long serialVersionUID = 8825508619301420378L;

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    public static Comparator<TimePoint> TIMEPOINT_COMPARATOR = new SerializableComparator<TimePoint>() {
        private static final long serialVersionUID = 7644726881387366025L;

        @Override
        public int compare(TimePoint o1, TimePoint o2) {
            long milliDiff = o1.asMillis() - o2.asMillis();
            return milliDiff<0 ?  -1 : milliDiff == 0 ? 0 : 1;
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
    public TimePoint plus(long milliseconds) {
        return new MillisecondsTimePoint(asMillis()+milliseconds);
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
        return dateFormatter.format(asDate());
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
