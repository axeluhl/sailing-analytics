package com.sap.sse.common.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class MultiTimeRangeImpl implements MultiTimeRange {
    private static final long serialVersionUID = -2440743542692297352L;
    private final TimeRange[] timeRanges;
    
    public MultiTimeRangeImpl(TimeRange... timeRanges) {
        this.timeRanges = minimizeAndSort(timeRanges);
    }
    
    public MultiTimeRangeImpl(Iterable<TimeRange> timeRanges) {
        this.timeRanges = new TimeRange[Util.size(timeRanges)];
        int i=0;
        for (final TimeRange timeRange : timeRanges) {
            this.timeRanges[i++] = timeRange;
        }
    }

    private TimeRange[] minimizeAndSort(TimeRange... timeRanges) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<TimeRange> iterator() {
        return Collections.unmodifiableCollection(Arrays.asList(timeRanges)).iterator();
    }

    @Override
    public MultiTimeRange add(MultiTimeRange other) {
        final List<TimeRange> newTimeRanges = new ArrayList<>();
        for (final TimeRange timeRange : timeRanges) {
            newTimeRanges.add(timeRange);
        }
        Util.addAll(other, newTimeRanges);
        return new MultiTimeRangeImpl(newTimeRanges);
    }

    @Override
    public MultiTimeRange add(TimeRange timeRange) {
        return add(new MultiTimeRangeImpl(timeRange));
    }

    /**
     * The implementation uses the fact that both multi time ranges' contained {@link TimeRange}s are ordered by
     * ascending {@link TimeRange#from() start} time points. For each of object's time ranges and each time range from
     * {@code other} that it overlaps with, one new {@link TimeRange} that represents the intersection is added to the
     * new result. Since between all adjacent {@link TimeRange} objects in each sequence there must have been a gap
     * (invariant of all {@link MultiTimeRange}s), the resulting sequence must also have a gap between all adjacent
     * ranges. 
     */
    @Override
    public MultiTimeRange intersect(MultiTimeRange other) {
        final Iterator<TimeRange> otherI = other.iterator();
        List<TimeRange> preResult = new ArrayList<>();
        if (otherI.hasNext()) {
            TimeRange nextRangeFromOther = otherI.next();
            for (final TimeRange nextRangeFromThis : this) {
                while (nextRangeFromOther.endsBefore(nextRangeFromThis.from())) {
                    if (otherI.hasNext()) {
                        nextRangeFromOther = otherI.next();
                    } else {
                        break; // all other time ranges ended before nextRangeFromThis; we're done
                    }
                }
                // Here, we have a valid nextRangeFromOther that does not end before nextRangeFromThis.
                // If it starts at or before nextRangeFromThis's start, we have an intersection.
                
            }
        }
        // TODO Auto-generated method stub
        return new MultiTimeRangeImpl(preResult);
    }

    @Override
    public MultiTimeRange intersect(TimeRange timeRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultiTimeRange subtract(MultiTimeRange other) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultiTimeRange subtract(TimeRange timeRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean includes(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean includes(TimeRange timeRange) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean includes(MultiTimeRange other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(timeRanges);
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
        MultiTimeRangeImpl other = (MultiTimeRangeImpl) obj;
        if (!Arrays.equals(timeRanges, other.timeRanges))
            return false;
        return true;
    }
}
