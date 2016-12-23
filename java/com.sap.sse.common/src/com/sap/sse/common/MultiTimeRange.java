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
    MultiTimeRange add(MultiTimeRange other);
    MultiTimeRange add(TimeRange timeRange);
    MultiTimeRange intersect(MultiTimeRange other);
    MultiTimeRange intersect(TimeRange timeRange);
    MultiTimeRange subtract(MultiTimeRange other);
    MultiTimeRange subtract(TimeRange timeRange);
    boolean includes(TimePoint timePoint);
    boolean includes(TimeRange timeRange);
    boolean includes(MultiTimeRange other);
    
    /**
     * Short for {@link Util#isEmpty(Iterable) Util.isEmpty(this)}
     */
    boolean isEmpty();
}
