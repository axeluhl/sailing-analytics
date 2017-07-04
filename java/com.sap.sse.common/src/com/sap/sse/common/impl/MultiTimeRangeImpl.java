package com.sap.sse.common.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
            if (!timeRange.isEmpty()) { // ignore empty time ranges
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
    public MultiTimeRange union(MultiTimeRange other) {
        final List<TimeRange> newTimeRanges = new ArrayList<>();
        for (final TimeRange timeRange : timeRanges) {
            newTimeRanges.add(timeRange);
        }
        Util.addAll(other, newTimeRanges);
        return new MultiTimeRangeImpl(newTimeRanges);
    }

    @Override
    public MultiTimeRange union(TimeRange timeRange) {
        return union(new MultiTimeRangeImpl(timeRange));
    }

    private TimeRange nextOrNull(Iterator<TimeRange> i) {
        final TimeRange result;
        if (i.hasNext()) {
            result = i.next();
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * The implementation uses the fact that both multi time ranges' contained {@link TimeRange}s are ordered by
     * ascending {@link TimeRange#from() start} time points. For each of {@code this} object's time ranges and each time
     * range from {@code other} that it overlaps with, one new {@link TimeRange} that represents the intersection is
     * added to the new result. Since between all adjacent {@link TimeRange} objects in each sequence there must have
     * been a gap (invariant of all {@link MultiTimeRange}s), the resulting sequence must also have a gap between all
     * adjacent ranges.
     */
    @Override
    public MultiTimeRange intersection(MultiTimeRange other) {
        List<TimeRange> preResult = new ArrayList<>();
        final Iterator<TimeRange> thisI = iterator();
        final Iterator<TimeRange> otherI = other.iterator();
        TimeRange thisRange = nextOrNull(thisI);
        TimeRange otherRange = nextOrNull(otherI);
        while (thisRange != null && otherRange != null) {
            if (thisRange.endsBefore(otherRange.from())) { // no intersection; move thisI forward
                thisRange = nextOrNull(thisI);
            } else if (otherRange.endsBefore(thisRange.from())) { // no intersection; move otherI forward
                otherRange = nextOrNull(otherI);
            } else {
                assert thisRange.from().compareTo(otherRange.to()) < 0 &&
                       thisRange.to().compareTo(otherRange.from()) > 0;
                // ...which, in short, is:
                assert thisRange.intersects(otherRange);
                final TimeRange intersection = thisRange.intersection(otherRange);
                assert intersection != null;
                preResult.add(intersection);
                // If thisRange been fully "consumed" by the otherRange move thisI forward:
                final boolean needToMoveThisI = !thisRange.to().after(otherRange.to());
                // If otherRange been fully "consumed" by the thisRange move otherI forward (IMPORTANT: decide before moving thisI/thisRange)
                final boolean needToMoveOtherI = !otherRange.to().after(thisRange.to());
                if (needToMoveThisI) {
                    thisRange = nextOrNull(thisI);
                }
                if (needToMoveOtherI) {
                    otherRange = nextOrNull(otherI);
                }
            }
        }
        return new MultiTimeRangeImpl(preResult);
    }

    @Override
    public MultiTimeRange intersection(TimeRange timeRange) {
        return intersection(new MultiTimeRangeImpl(timeRange));
    }

    @Override
    public boolean intersects(MultiTimeRange other) {
        return !intersection(other).isEmpty();
    }

    @Override
    public boolean intersects(TimeRange other) {
        return !intersection(other).isEmpty();
    }

    @Override
    public MultiTimeRange subtract(MultiTimeRange other) {
        // loops over the two time range sequences; when a time range in this multi time range is skipped because it does not
        // intersect with any time range from other, add it; subtracting time ranges that do not intersect with any time range
        // from this are a no-op.
        List<TimeRange> preResult = new ArrayList<>();
        final Iterator<TimeRange> thisI = iterator();
        final Iterator<TimeRange> otherI = other.iterator();
        TimeRange thisRange = nextOrNull(thisI);
        TimeRange otherRange = nextOrNull(otherI);
        Set<TimeRange> unaffectedTimeRangesFromThis = new HashSet<>();
        Util.addAll(this, unaffectedTimeRangesFromThis);
        while (thisRange != null && otherRange != null) {
            if (thisRange.endsBefore(otherRange.from())) { // no intersection; move thisI forward
                thisRange = nextOrNull(thisI);
            } else if (otherRange.endsBefore(thisRange.from())) { // no intersection; move otherI forward
                otherRange = nextOrNull(otherI);
            } else {
                assert thisRange.from().compareTo(otherRange.to()) < 0 &&
                       thisRange.to().compareTo(otherRange.from()) > 0;
                // ...which, in short, is:
                assert thisRange.intersects(otherRange);
                unaffectedTimeRangesFromThis.remove(thisRange); // we cut off a bit from thisRange; don't add unchanged
                // now add all subtraction results except for the last one in case it ends after otherRange because
                // in that case the next otherRange candidate may chop off something or split this remaining part again
                for (final TimeRange subtractResult : thisRange.subtract(otherRange)) {
                    if (subtractResult.to().before(thisRange.to())) {
                        preResult.add(subtractResult);
                    } else {
                        thisRange = subtractResult;
                    }
                }
                // If thisRange been fully "consumed" by the otherRange move thisI forward:
                final boolean needToMoveThisI = !thisRange.to().after(otherRange.to());
                // If otherRange been fully "consumed" by the thisRange move otherI forward (IMPORTANT: decide before moving thisI/thisRange)
                final boolean needToMoveOtherI = !otherRange.to().after(thisRange.to());
                if (needToMoveThisI) {
                    thisRange = nextOrNull(thisI);
                }
                if (needToMoveOtherI) {
                    otherRange = nextOrNull(otherI);
                }
            }
        }
        preResult.addAll(unaffectedTimeRangesFromThis);
        if (thisRange != null) {
            preResult.add(thisRange); // a left-over of this multi range that wasn't canceled out by any of the other time ranges
        }
        return new MultiTimeRangeImpl(preResult);
    }

    @Override
    public MultiTimeRange subtract(TimeRange timeRange) {
        return subtract(new MultiTimeRangeImpl(timeRange));
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
        for (final TimeRange tr : this) {
            if (tr.includes(timeRange)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean includes(MultiTimeRange other) {
        for (final TimeRange otherTimeRange : other) {
            if (!includes(otherTimeRange)) {
                return false;
            }
        }
        return true;
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

    @Override
    public String toString() {
        return Arrays.toString(timeRanges);
    }
}
