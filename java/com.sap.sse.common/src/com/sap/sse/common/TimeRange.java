package com.sap.sse.common;

import java.io.Serializable;

public interface TimeRange extends Comparable<TimeRange>, Serializable {
    /**
     * @return {@code null}, if the timepoint is at the beginning of time
     */
    TimePoint from();
    
    /**
     * @return {@code null}, if the timepoint is at the beginning of time
     */
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
     * Is the time range open ended towards older timepoints?
     */
    boolean openBeginning();
    
    /**
     * Is the time range open ended towards newer timepoints?
     */
    boolean openEnd();
    
    /**
     * Get time-difference between the timepoint and this timerange.
     * It will be 0 if the timepoint lies within the timerange, otherwise the time-difference
     * to either the start or end of the timerange, depending on which is closer. The
     * time difference is a duration that is guaranteed to be positive.
     */
    Duration timeDifference(TimePoint timePoint);
    
    /**
     * Merges the two ranges, only possible if {@code other} {@link #intersects()} this range.
     */
    TimeRange union(TimeRange other);
    
    /**
     * Intersection of the two ranges, only possible if {@code other} {@link #intersects()} this range.
     */
    TimeRange intersection(TimeRange other);
}