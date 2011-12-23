package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.util.impl.AbstractUnmodifiableNavigableSet;
import com.sap.sailing.util.impl.DescendingNavigableSet;

/**
 * A virtual wind track that computes the wind bearing based on the boat tracks recorded in the tracked
 * race for which this wind track is constructed. It has a fixed time resolution as defined by the constant
 * {@link #RESOLUTION_IN_MILLISECONDS}. When asked for the wind at a time at which the wind cannot be estimated,
 * the raw fixes will have <code>null</code> as value for this time. These <code>null</code> "fixes" are at the
 * same time considered "outliers" by the {@link #getInternalFixes()} operation which filters them from the
 * "smoothened" view. With this, the view with "outliers" removed contains all those fixes for which the wind
 * bearing was successfully computed from the tracked race's boat tracks.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackBasedEstimationWindTrackImpl extends WindTrackImpl {
    /**
     * The time resolution is one second.
     */
    private static final long RESOLUTION_IN_MILLISECONDS = 1000l;
    
    private final EstimatedWindFixesAsNavigableSet virtualInternalRawFixes;
    
    public TrackBasedEstimationWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage) {
        super(millisecondsOverWhichToAverage);
        this.virtualInternalRawFixes = new EstimatedWindFixesAsNavigableSet(trackedRace);
    }
    
    @Override
    protected NavigableSet<Wind> getInternalRawFixes() {
        return virtualInternalRawFixes;
    }
    
    @Override
    protected NavigableSet<Wind> getInternalFixes() {
        return new PartialNavigableSetView<Wind>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(Wind e) {
                return e != null;
            }
        };
    }

    /**
     * Emulates a collection of {@link Wind} fixes for a {@link TrackedRace}, computed using
     * {@link TrackedRace#getEstimatedWindDirection(com.sap.sailing.domain.base.Position, TimePoint)}. If not contrained
     * by a {@link #from} and/or a {@link #to} time point, an equidistant time field is assumed, starting at
     * {@link TrackedRace#getStart()} and leading up to {@link TrackedRace#getTimePointOfNewestEvent()}. If
     * {@link TrackedRace#getStart()} returns <code>null</code>, {@link Long#MAX_VALUE} is used as the {@link #from}
     * time point, pushing the start to the more or less infinite future ("end of the universe"). If no event was
     * received yet and hence {@link TrackedRace#getTimePointOfNewestEvent()} returns <code>null</code>, the
     * {@link #to} end is assumed to be the beginning of the epoch (1970-01-01T00:00:00).
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private static class EstimatedWindFixesAsNavigableSet extends AbstractUnmodifiableNavigableSet<Wind> {
        private final TrackedRace trackedRace;
        
        private final TimePoint from;
        
        private final TimePoint to;
        
        public EstimatedWindFixesAsNavigableSet(TrackedRace trackedRace) {
            this(trackedRace, null, null);
        }
        
        private EstimatedWindFixesAsNavigableSet(TrackedRace trackedRace, TimePoint from, TimePoint to) {
            this.trackedRace = trackedRace;
            this.from = from;
            this.to = to;
        }
        
        private TimePoint lowerToResolution(Wind w) {
            return new MillisecondsTimePoint((w.getTimePoint().asMillis()-1) / RESOLUTION_IN_MILLISECONDS * RESOLUTION_IN_MILLISECONDS);
        }

        private TimePoint floorToResolution(Wind w) {
            return new MillisecondsTimePoint(w.getTimePoint().asMillis() / RESOLUTION_IN_MILLISECONDS * RESOLUTION_IN_MILLISECONDS);
        }

        private TimePoint ceilingToResolution(Wind w) {
            return new MillisecondsTimePoint(((w.getTimePoint().asMillis()-1) / RESOLUTION_IN_MILLISECONDS + 1) * RESOLUTION_IN_MILLISECONDS);
        }

        private TimePoint higherToResolution(Wind w) {
            return new MillisecondsTimePoint((w.getTimePoint().asMillis() / RESOLUTION_IN_MILLISECONDS + 1) * RESOLUTION_IN_MILLISECONDS);
        }
        
        private TimePoint getFrom() {
            return from == null ? trackedRace.getStart() == null ? new MillisecondsTimePoint(Long.MAX_VALUE)
                    : trackedRace.getStart() : from;
        }
        
        private TimePoint getTo() {
            return to == null ? trackedRace.getTimePointOfNewestEvent() == null ? new MillisecondsTimePoint(0)
                    : trackedRace.getTimePointOfNewestEvent() : to;
        }

        @Override
        public Wind lower(Wind w) {
            TimePoint timePoint = lowerToResolution(w);
            return timePoint.compareTo(getFrom()) < 0 ? null : trackedRace.getEstimatedWindDirection(w.getPosition(), timePoint);
        }

        @Override
        public Wind floor(Wind w) {
            TimePoint timePoint = floorToResolution(w);
            return timePoint.compareTo(getFrom()) < 0 ? null : trackedRace.getEstimatedWindDirection(w.getPosition(), timePoint);
        }

        @Override
        public Wind ceiling(Wind w) {
            TimePoint timePoint = ceilingToResolution(w);
            return timePoint.compareTo(getTo()) > 0 ? null : trackedRace.getEstimatedWindDirection(w.getPosition(), timePoint);
        }

        @Override
        public Wind higher(Wind w) {
            TimePoint timePoint = higherToResolution(w);
            return timePoint.compareTo(getTo()) > 0 ? null : trackedRace.getEstimatedWindDirection(w.getPosition(), timePoint);
        }

        @Override
        public Iterator<Wind> iterator() {
            return new Iterator<Wind>() {
                private TimePoint timePoint = getFrom();

                @Override
                public boolean hasNext() {
                    return timePoint.compareTo(getTo()) < 0;
                }

                @Override
                public Wind next() {
                    Wind result = floor(new DummyWind(timePoint));
                    timePoint = new MillisecondsTimePoint(timePoint.asMillis()+RESOLUTION_IN_MILLISECONDS);
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
            return descendingSet().iterator();
        }

        @Override
        public NavigableSet<Wind> subSet(Wind fromElement, boolean fromInclusive, Wind toElement, boolean toInclusive) {
            return new EstimatedWindFixesAsNavigableSet(trackedRace, fromInclusive?ceilingToResolution(fromElement):higherToResolution(fromElement),
                    toInclusive?floorToResolution(toElement):lowerToResolution(toElement));
        }

        @Override
        public NavigableSet<Wind> headSet(Wind toElement, boolean inclusive) {
            return new EstimatedWindFixesAsNavigableSet(trackedRace, /* from */ null,
                    inclusive?floorToResolution(toElement):lowerToResolution(toElement));
        }

        @Override
        public NavigableSet<Wind> tailSet(Wind fromElement, boolean inclusive) {
            return new EstimatedWindFixesAsNavigableSet(trackedRace, inclusive?ceilingToResolution(fromElement):higherToResolution(fromElement),
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
            return (int) ((getTo().asMillis()-getFrom().asMillis())/RESOLUTION_IN_MILLISECONDS);
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
            int i=0;
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
            int i=0;
            for (Wind w : this) {
                result[i++] = w;
            }
            @SuppressWarnings("unchecked")
            T[] tResult = (T[]) result;
            return tResult;
        }
    }
}
