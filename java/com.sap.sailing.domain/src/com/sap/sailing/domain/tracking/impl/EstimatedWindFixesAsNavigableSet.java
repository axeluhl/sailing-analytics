package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl.DummyWind;
import com.sap.sailing.util.impl.AbstractUnmodifiableNavigableSet;
import com.sap.sailing.util.impl.DescendingNavigableSet;

/**
 * Emulates a collection of {@link Wind} fixes for a {@link TrackedRace}, computed using
 * {@link TrackedRace#getEstimatedWindDirection(com.sap.sailing.domain.base.Position, TimePoint)}. If not constrained
 * by a {@link #from} and/or a {@link #to} time point, an equidistant time field is assumed, starting at
 * {@link TrackedRace#getStart()} and leading up to {@link TrackedRace#getTimePointOfNewestEvent()}. If
 * {@link TrackedRace#getStart()} returns <code>null</code>, {@link Long#MAX_VALUE} is used as the {@link #from}
 * time point, pushing the start to the more or less infinite future ("end of the universe"). If no event was
 * received yet and hence {@link TrackedRace#getTimePointOfNewestEvent()} returns <code>null</code>, the {@link #to}
 * end is assumed to be the beginning of the epoch (1970-01-01T00:00:00).
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class EstimatedWindFixesAsNavigableSet extends AbstractUnmodifiableNavigableSet<Wind> {
    /**
     * The time resolution is one second.
     */
    private static final long RESOLUTION_IN_MILLISECONDS = 1000l;

    private final TrackedRace trackedRace;

    private final TimePoint from;

    private final TimePoint to;
    
    private final TrackBasedEstimationWindTrackImpl track;

    public EstimatedWindFixesAsNavigableSet(TrackBasedEstimationWindTrackImpl track, TrackedRace trackedRace) {
        this(track, trackedRace, null, null);
    }
    
    public long getResolutionInMilliseconds() {
        return RESOLUTION_IN_MILLISECONDS;
    }

    /**
     * @param from expected to be an integer multiple of {@link #RESOLUTION_IN_MILLISECONDS} or <code>null</code>
     * @param to expected to be an integer multiple of {@link #RESOLUTION_IN_MILLISECONDS} or <code>null</code>
     */
    private EstimatedWindFixesAsNavigableSet(TrackBasedEstimationWindTrackImpl track, TrackedRace trackedRace,
            TimePoint from, TimePoint to) {
        this.track = track;
        this.trackedRace = trackedRace;
        assert from == null || from.asMillis() % RESOLUTION_IN_MILLISECONDS == 0;
        this.from = from;
        assert to == null || to.asMillis() % RESOLUTION_IN_MILLISECONDS == 0;
        this.to = to;
    }

    private TimePoint lowerToResolution(Wind w) {
        TimePoint result;
        final TimePoint timePointOfLastEvent = trackedRace.getTimePointOfLastEvent();
        if (timePointOfLastEvent == null) {
            // nothing received yet; "lowering" to end of time
            result = new MillisecondsTimePoint((Long.MAX_VALUE - 1) / RESOLUTION_IN_MILLISECONDS
                    * RESOLUTION_IN_MILLISECONDS);
        } else if (w.getTimePoint().compareTo(timePointOfLastEvent) > 0) {
            result = lowerToResolution(new DummyWind(timePointOfLastEvent));
        } else {
            result = new MillisecondsTimePoint((w.getTimePoint().asMillis() - 1) / RESOLUTION_IN_MILLISECONDS
                    * RESOLUTION_IN_MILLISECONDS);
        }
        return result;
    }

    private TimePoint floorToResolution(Wind w) {
        TimePoint result;
        final TimePoint timePointOfLastEvent = trackedRace.getTimePointOfLastEvent();
        if (timePointOfLastEvent == null) {
            // nothing received yet; "lowering" to end of time
            result = new MillisecondsTimePoint((Long.MAX_VALUE - 1) / RESOLUTION_IN_MILLISECONDS
                    * RESOLUTION_IN_MILLISECONDS);
        } else if (w.getTimePoint().compareTo(timePointOfLastEvent) > 0) {
            result = floorToResolution(new DummyWind(timePointOfLastEvent));
        } else {
            result = new MillisecondsTimePoint(w.getTimePoint().asMillis() / RESOLUTION_IN_MILLISECONDS
                    * RESOLUTION_IN_MILLISECONDS);
        }
        return result;
    }

    private TimePoint ceilingToResolution(Wind w) {
        TimePoint result;
        final TimePoint startOfTracking = trackedRace.getStartOfTracking();
        if (startOfTracking == null) {
            // no start of tracking yet; "ceiling" to beginning of time
            result = new MillisecondsTimePoint(0);
        } else if (w.getTimePoint().compareTo(startOfTracking) < 0) {
            result = ceilingToResolution(new DummyWind(startOfTracking));
        } else {
            result = new MillisecondsTimePoint(((w.getTimePoint().asMillis() - 1) / RESOLUTION_IN_MILLISECONDS + 1)
                    * RESOLUTION_IN_MILLISECONDS);
        }
        return result;
    }

    private TimePoint higherToResolution(Wind w) {
        TimePoint result;
        final TimePoint startOfTracking = trackedRace.getStartOfTracking();
        if (startOfTracking == null) {
            // no start of tracking yet; "ceiling" to beginning of time
            result = new MillisecondsTimePoint(0);
        } else if (w.getTimePoint().compareTo(startOfTracking) < 0) {
            result = higherToResolution(new DummyWind(startOfTracking));
        } else {
            result = new MillisecondsTimePoint((w.getTimePoint().asMillis() / RESOLUTION_IN_MILLISECONDS + 1)
                    * RESOLUTION_IN_MILLISECONDS);
        }
        return result;
    }

    /**
     * The time point starting from and including which the GPS fixes are considered in the race's tracks. Returns the
     * value of {@link #from} unless it is <code>null</code>. In this case, the time point of the
     * {@link TrackedRace#getStart() race start}, {@link #floorToResolution(Wind) floored to the resolution of this set}
     * will be returned instead. If no valid start time can be obtained from the race, <code>Long.MAX_VALUE</code> is
     * returned instead.
     */
    private TimePoint getFrom() {
        return from == null ? trackedRace.getStart() == null ? new MillisecondsTimePoint(Long.MAX_VALUE)
                : floorToResolution(new DummyWind(trackedRace.getStart())) : from;
    }

    /**
     * Time point up to and including which the GPS fixes are considered in the race's tracks. Returns the value of
     * {@link #to} unless it is <code>null</code>. In this case, the time point of the
     * {@link TrackedRace#getTimePointOfNewestEvent() time point of the newest event},
     * {@link #ceilingToResolution(Wind) ceiled to the resolution of this set} will be returned instead. If no valid
     * time of a newest event can be obtained from the race, <code>MillisecondsTimePoint(1)</code> is returned instead.
     */
    private TimePoint getTo() {
        return to == null ? trackedRace.getTimePointOfNewestEvent() == null ? new MillisecondsTimePoint(1)
                : ceilingToResolution(new DummyWind(trackedRace.getTimePointOfNewestEvent())) : to;
    }

    @Override
    public Wind lower(Wind w) {
        TimePoint timePoint = lowerToResolution(w);
        return timePoint.compareTo(getFrom()) < 0 ? null : track.getEstimatedWindDirection(w.getPosition(), timePoint);
    }

    @Override
    public Wind floor(Wind w) {
        TimePoint timePoint = floorToResolution(w);
        return timePoint.compareTo(getFrom()) < 0 ? null : track.getEstimatedWindDirection(w.getPosition(), timePoint);
    }

    @Override
    public Wind ceiling(Wind w) {
        TimePoint timePoint = ceilingToResolution(w);
        return timePoint.compareTo(getTo()) > 0 ? null : track.getEstimatedWindDirection(w.getPosition(), timePoint);
    }

    @Override
    public Wind higher(Wind w) {
        TimePoint timePoint = higherToResolution(w);
        return timePoint.compareTo(getTo()) > 0 ? null : track.getEstimatedWindDirection(w.getPosition(), timePoint);
    }

    @Override
    public Iterator<Wind> iterator() {
        return new Iterator<Wind>() {
            private TimePoint timePoint = getFrom();

            @Override
            public boolean hasNext() {
                return timePoint.compareTo(getTo()) <= 0;
            }

            @Override
            public Wind next() {
                Wind result = floor(new DummyWind(timePoint));
                timePoint = new MillisecondsTimePoint(timePoint.asMillis() + RESOLUTION_IN_MILLISECONDS);
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<Wind> descendingSet() {
        return new DescendingNavigableSet<Wind>(this);
    }

    @Override
    public Iterator<Wind> descendingIterator() {
        return new Iterator<Wind>() {
            private TimePoint timePoint = lowerToResolution(new DummyWind(getTo()));

            @Override
            public boolean hasNext() {
                return timePoint.compareTo(getFrom()) >= 0;
            }

            @Override
            public Wind next() {
                Wind result = floor(new DummyWind(timePoint));
                timePoint = new MillisecondsTimePoint(timePoint.asMillis() - RESOLUTION_IN_MILLISECONDS);
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<Wind> subSet(Wind fromElement, boolean fromInclusive, Wind toElement, boolean toInclusive) {
        return new EstimatedWindFixesAsNavigableSet(track, trackedRace, fromInclusive ? ceilingToResolution(fromElement)
                : higherToResolution(fromElement), toInclusive ? floorToResolution(toElement)
                : lowerToResolution(toElement));
    }

    @Override
    public NavigableSet<Wind> headSet(Wind toElement, boolean inclusive) {
        return new EstimatedWindFixesAsNavigableSet(track, trackedRace, /* from */ null,
                inclusive ? ceilingToResolution(toElement) : lowerToResolution(toElement));
    }

    @Override
    public NavigableSet<Wind> tailSet(Wind fromElement, boolean inclusive) {
        return new EstimatedWindFixesAsNavigableSet(track, trackedRace, inclusive ? floorToResolution(fromElement)
                : higherToResolution(fromElement),
        /* to */ null);
    }

    @Override
    public SortedSet<Wind> subSet(Wind fromElement, Wind toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<Wind> headSet(Wind toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<Wind> tailSet(Wind fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super Wind> comparator() {
        return WindComparator.INSTANCE;
    }

    @Override
    public Wind first() {
        return floor(new DummyWind(getFrom()));
    }

    @Override
    public Wind last() {
        return ceiling(new DummyWind(getTo()));
    }

    @Override
    public int size() {
        return (int) ((getTo().asMillis() - getFrom().asMillis()) / RESOLUTION_IN_MILLISECONDS);
    }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if (o instanceof Wind) {
            Wind wind = (Wind) o;
            result = wind.getTimePoint().asMillis() % RESOLUTION_IN_MILLISECONDS == 0
                    && wind.getTimePoint().compareTo(getFrom()) >= 0 && wind.getTimePoint().compareTo(getTo()) < 0;
        }
        return result;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size()];
        int i = 0;
        for (Wind w : this) {
            result[i++] = w;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] result = a;
        if (result.length < size()) {
            result = new Object[size()];
        }
        int i = 0;
        for (Wind w : this) {
            result[i++] = w;
        }
        @SuppressWarnings("unchecked")
        T[] tResult = (T[]) result;
        return tResult;
    }
}

