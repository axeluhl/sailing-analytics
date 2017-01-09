package com.sap.sse.common;

import java.io.Serializable;

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
    
    boolean startsAfter(TimePoint timePoint);
    
    boolean endsAfter(TimeRange other);
    
    boolean endsBefore(TimePoint timePoint);
    
    /**
     * Is the time range open ended towards older timepoints?
     */
    boolean hasOpenBeginning();
    
    /**
     * Is the time range open ended towards newer timepoints?
     */
    boolean hasOpenEnd();
    
    /**
     * Get time-difference between the timepoint and this timerange.
     * It will be 0 if the timepoint lies within the timerange, otherwise the time-difference
     * to either the start or end of the timerange, depending on which is closer. The
     * time difference is a duration that is guaranteed to be positive.
     */
    Duration timeDifference(TimePoint timePoint);
    
    /**
     * Merges the two ranges, only possible if {@code other} {@link #intersects()} this range.
     * 
     * @return the union of this and the {@code other} time range if they {@link #intersects(TimeRange) intersect} which
     *         means that the result {@link #includes(TimeRange) includes} both this and the {@code other} time range;
     *         {@code null} otherwise
     */
    TimeRange union(TimeRange other);
    
    /**
     * Intersection of the two ranges, only possible if {@code other} {@link #intersects()} this range.
     */
    TimeRange intersection(TimeRange other);
    
    /**
     * Returns zero, one or two {@link TimeRange}s such that no {@link TimePoint} that is {@link #includes(TimePoint)
     * contained} in {@code other} is contained in any of the {@link TimeRange}s returned and that all {@link TimePoint}s
     * that are contained in {@code this} {@link TimeRange} are contained in exactly one of the {@link TimeRange}s
     * returned, and that the {@link TimeRange}s returned do not {@link #intersects(TimeRange) intersect}. Furthermore,
     * in case two {@link TimeRange}s are returned, the first one {@link #startsBefore(TimeRange) is before} the second
     * one.
     */
    TimeRange[] subtract(TimeRange other);
}