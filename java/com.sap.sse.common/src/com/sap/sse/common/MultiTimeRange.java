package com.sap.sse.common;

import java.io.Serializable;

/**
 * A minimal sequence of non-overlapping, non-touching, non-{@link TimeRange#isEmpty() empty} {@link TimeRange} objects.
 * The iteration order is from earlier to later time range. An object of this type may be empty. Only the last
 * {@link TimeRange} in the iteration may be unbounded at its end, and only the first {@link TimeRange} in the iteration
 * may be unbounded at its start. Being minimal means that no other {@link MultiTimeRange} can represent the exact same
 * set of {@link TimePoint}s with fewer {@link TimeRange} elements in it.
 * <p>
 * 
 * Two {@link MultiTimeRange} objects are equal if they {@link #includes(TimePoint)} the same set of {@link TimePoint}s.
 * Combined with the minimality requirement, implying unique representations of {@link MultiTimeRange} objects equal in
 * that sense, the comparison can happen based on the equality of the {@link TimeRange}s in the iteration.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MultiTimeRange extends Iterable<TimeRange>, Serializable {
    /**
     * @return a multi time range that {@link #includes(MultiTimeRange) includes} the {@code other} multi time range and
     *         {@code this}, and that {@link #includes(TimePoint) includes} only time points that are
     *         {@link #includes(TimePoint) included} in {@code other} or in {@code this}. Conversely, a
     *         {@link TimePoint} that is not {@link #includes(TimePoint) included} in either {@code other} nor
     *         {@code this} will neither be {@link #includes(TimePoint) included} in the result.
     */
    MultiTimeRange union(MultiTimeRange other);

    /**
     * @return a multi time range that {@link #includes(TimeRange) includes} the {@code other} time range and
     *         {@link #includes(MultiTimeRange) includes} {@code this}, and that {@link #includes(TimePoint) includes}
     *         only time points that are {@link TimeRange#includes(TimePoint) included} in {@code other} or that are
     *         {@link #includes(TimePoint) included} in {@code this}. Conversely, a {@link TimePoint} that is not
     *         {@link TimeRange#includes(TimePoint) included} in either {@code other} nor {@link #includes(TimePoint)
     *         included} in {@code this} will neither be {@link #includes(TimePoint) included} in the result.
     */
    MultiTimeRange union(TimeRange timeRange);

    /**
     * @return a multi time range {@code result} such that for all {@link TimePoint}s {@code t} with
     *         {@link #includes(TimePoint) result.includes(t)} we also have {@link #includes(TimePoint)
     *         other.includes(t)} {@code &&} {@link #includes(TimePoint) this.includes(t)} and for all {@link TimePoint}
     *         s not in {@code result} they are either not in {@code other} or not in {@code this}.
     */
    MultiTimeRange intersection(MultiTimeRange other);

    /**
     * Same as {@link #intersection(MultiTimeRange)} with a multi time range consisting only of {@code timeRange}
     */
    MultiTimeRange intersection(TimeRange timeRange);
    
    /**
     * @return {@code false} if and only if {@link #intersection(MultiTimeRange) intersection(other)}.{@link #isEmpty()}
     */
    boolean intersects(MultiTimeRange other);
    
    /**
     * Same as {@link #intersects(MultiTimeRange)} with a multi time range consisting only of {@code timeRange}
     */
    boolean intersects(TimeRange other);

    /**
     * @return a multi time range such that the result {@link #includes(TimePoint) includes} all time points that are
     *         {@link #includes(TimePoint) contained} in {@code this} multi time range but are not
     *         {@link #includes(TimePoint) contained} in {@code other}.
     */
    MultiTimeRange subtract(MultiTimeRange other);
    
    /**
     * Same as {@link #subtract(MultiTimeRange)} with a multi time range consisting only of {@code timeRange}
     */
    MultiTimeRange subtract(TimeRange timeRange);

    /**
     * @return {@code true} if and only if exactly one of the time ranges in this multi time range
     *         {@link TimeRange#includes(TimePoint) includes} the {@code timePoint}
     */
    boolean includes(TimePoint timePoint);

    /**
     * @return {@code true} if and only exactly one of the time ranges in this multi time range
     *         {@link TimeRange#includes(TimeRange) includes} the {@code timeRange}.
     */
    boolean includes(TimeRange timeRange);

    /**
     * @return {@code true} if and only if all {@link TimeRange}s from the {@code other} time range are
     *         {@link #includes(TimeRange) included} by {@code this} multi time range
     */
    boolean includes(MultiTimeRange other);

    /**
     * Short for {@link Util#isEmpty(Iterable) Util.isEmpty(this)}
     */
    boolean isEmpty();
}
