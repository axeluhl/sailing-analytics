package com.sap.sse.common;

import java.io.Serializable;

/**
 * A range between two {@link TimePoint}s, including the {@link #from()} time point and excluding the {@link #to} time
 * point. A time range is {@link #isEmpty()} if its {@link #from()} and {@link #to()} are equal.
 * Time ranges never have a {@link #from()} that is {@link TimePoint#after(TimePoint) after} {@link #to()}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TimeRange extends Comparable<TimeRange>, Serializable {
    /**
     * @return a valid, non-{@code null} time point marking the inclusive beginning of this time range.
     * {@link #includes(TimePoint) includes(from())} always returns {@code true}.
     */
    TimePoint from();
    
    /**
     * @return a valid, non-{@code null} time point marking the exclusive end of this time range;
     * {@link #includes(TimePoint) includes(to())} always returns {@code false}.
     */
    TimePoint to();
    
    boolean isEmpty();
    
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
    
    /**
     * @return {@code true} if and only if at least one {@link TimePoint} exists that is {@link #includes(TimePoint) included} in
     * both, {@code this} and the {@code other} time range. This in particular means that an {@link #isEmpty() empty}
     * time range intersects with no other time range and nothing intersects with an {@link #isEmpty() empty} time range.
     * 
     * @see #touches(TimeRange)
     */
    boolean intersects(TimeRange other);
    
    /**
     * @return {@code true} if and only if {@code this} time range {@link #intersects(TimeRange)} with the {@code other} time range
     * or its {@link #to() exclusive end} equals the {@code other} time range's {@link #from() start}, or the {@code other} time
     * range's {@link #to() end} equals {@code this} time range's {@link #from() start}.
     */
    boolean touches(TimeRange other);
    
    boolean startsBefore(TimeRange other);
    
    boolean startsAtOrAfter(TimePoint timePoint);
    
    boolean startsAfter(TimeRange other);
    
    boolean endsAfter(TimeRange other);
    
    /**
     * @return {@code true} if and only if this range's {@link #to() exclusive end} is at or before {@code timePoint}
     */
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
     * Get time-difference between the timepoint and this timerange. It will be 0 if the timepoint lies within the
     * timerange, otherwise the time-difference to either the start or end of the timerange, depending on which is
     * closer. The time difference is a duration that is guaranteed to be positive. Although the end of a time range is
     * exclusive, a {@link TimePoint} that equals the {@link #to()} time point of this time range is defined to have a
     * {@link Duration#NULL zero duration} as conceptually the time range ends infinitely close to the {@link #to()}
     * time point, regardless the resolution of the {@link TimePoint} implementation chosen.
     */
    Duration timeDifference(TimePoint timePoint);
    
    /**
     * Merges the two ranges, only possible if {@code other} {@link #touches()} this range.<p>
     * 
     * If you want to join two or more {@link TimeRange} objects, consider using {@link MultiTimeRange} instead.
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
    MultiTimeRange subtract(TimeRange other);

    /**
     * The duration between the {@link #from() start} and the {@link #to() end} of this time range.
     * Short for {@link #from()}.{@link TimePoint#until(TimePoint) until}({@link #to()}).
     */
    Duration getDuration();
}