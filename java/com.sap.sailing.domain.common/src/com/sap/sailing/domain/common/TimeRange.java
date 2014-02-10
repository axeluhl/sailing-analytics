package com.sap.sailing.domain.common;

import java.io.Serializable;

/**
 * A TimeRange defined by a starting timepoint {@link #from()}, and an ending timepoint {@link #to()}, which are both inclusive.
 * @author Fredrik Teschke
 *
 */
public interface TimeRange extends Comparable<TimeRange>, Serializable {
    TimePoint from();
    TimePoint to();
    
    /**
     * Also returns true if (either or both) the from or to timepoints are equal.
     * E.g. 10-100 lies within 10-00, and 10-50 lies within 10-100.
     * @param other
     * @return
     */
    boolean liesWithin(TimeRange other);
    
    /**
     * Reverse operation of {@link #liesWithin(TimeRange)}.
     * x.includes(y) = y.liesWithin(x)
     * @param other
     * @return
     */
    boolean includes(TimeRange other);
    
    boolean includes(TimePoint timePoint);
    
    boolean intersects(TimeRange other);
    
    boolean startsBefore(TimeRange other);
    
    boolean endsAfter(TimeRange other);
    
    /**
     * Get time-difference between the timepoint and this timerange.
     * It will be 0 if the timepoint lies within the timerange, otherwise the time-difference
     * to either the start or end of the timerange, depending on which is closer
     * @param timePoint
     * @return
     */
    long timeDifference(TimePoint timePoint);
}