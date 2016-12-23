package com.sap.sse.common.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.common.MultiTimeRange;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class MultiTimeRangeImpl implements MultiTimeRange {
    private static class TimeRangeByStartTimeComparator implements Comparator<TimeRange> {
        @Override
        public int compare(TimeRange tr1, TimeRange tr2) {
            return tr1.from().compareTo(tr2.from());
        }
    }

    private static final long serialVersionUID = -2440743542692297352L;
    private final TimeRange[] timeRanges;
    
    public MultiTimeRangeImpl(TimeRange... timeRanges) {
        this.timeRanges = minimizeAndSort(timeRanges);
    }
    
    public MultiTimeRangeImpl(Iterable<TimeRange> timeRanges) {
        final TimeRange[] timeRangesAsArray = new TimeRange[Util.size(timeRanges)];
        int i=0;
        for (final TimeRange timeRange : timeRanges) {
            timeRangesAsArray[i++] = timeRange;
        }
        this.timeRanges = minimizeAndSort(timeRangesAsArray);
    }

    private TimeRange[] minimizeAndSort(TimeRange... timeRanges) {
        final TimeRange[] sortedTimeRanges = new TimeRange[timeRanges.length];
        System.arraycopy(timeRanges, 0, sortedTimeRanges, 0, timeRanges.length);
        Arrays.sort(sortedTimeRanges, new TimeRangeByStartTimeComparator());
        final List<TimeRange> minimalTimeRanges = new ArrayList<>();
        TimeRange lastAdded = null;
        for (final TimeRange timeRange : sortedTimeRanges) {
            if (lastAdded == null) {
                lastAdded = timeRange;
                minimalTimeRanges.add(lastAdded);
            } else {
                if (timeRange.touches(lastAdded)) {
                    // ranges touch or even overlap; join into one:
                    lastAdded = lastAdded.union(timeRange);
                    minimalTimeRanges.set(minimalTimeRanges.size()-1, lastAdded); // replace
                } else {
                    // Since the time ranges are sorted by ascending start time and because timeRange
                    // does not touch lastAdded, timeRange must be after lastAdded, with a gap in between.
                    // Add timeRange as the next element:
                    lastAdded = timeRange;
                    minimalTimeRanges.add(lastAdded);
                }
            }
            assert lastAdded != null;
            if (lastAdded.to().equals(TimePoint.EndOfTime)) {
                break; // nothing more to minimize; the last time range added extends until the end of time
            }
        }
        return minimalTimeRanges.toArray(new TimeRange[minimalTimeRanges.size()]);
    }

    @Override
    public Iterator<TimeRange> iterator() {
        return Collections.unmodifiableCollection(Arrays.asList(timeRanges)).iterator();
    }

    @Override
    public boolean isEmpty() {
        return timeRanges.length == 0;
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
        // do a binary search for timePoint by looking for the last time range in timeRanges that starts at or before timePoint
        int pos = Arrays.binarySearch(timeRanges, 0, timeRanges.length, new TimeRangeImpl(timePoint, null),
                new TimeRangeByStartTimeComparator());
        if (pos < 0) { // no exact match; -pos-1 would be "insertion point", but in this case
            // not having an exact match means that we're interested in the last time range that
            // starts before timePoint, so we'll subtract one from -pos-1, resulting in -pos-2. This may
            // then be -1 in case there is no time range starting before timePoint.
            pos = -pos-2;
        }
        assert pos==-1 || timeRanges[pos].from().compareTo(timePoint) <= 0;
        return pos >= 0 && timePoint.before(timeRanges[pos].to());
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
