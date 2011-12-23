package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.util.impl.ArrayListNavigableSet;
import com.sap.sailing.util.impl.UnmodifiableNavigableSet;

public abstract class TrackImpl<FixType extends Timed> implements Track<FixType> {
    /**
     * The fixes, ordered by their time points
     */
    private final NavigableSet<Timed> fixes;
    
    protected class DummyTimed implements Timed {
        private final TimePoint timePoint;
        public DummyTimed(TimePoint timePoint) {
            super();
            this.timePoint = timePoint;
        }
        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }
        @Override
        public String toString() {
            return timePoint.toString();
        }
    }
    
    public TrackImpl() {
        this(new ArrayListNavigableSet<Timed>(TimedComparator.INSTANCE));
    }
    
    protected TrackImpl(NavigableSet<Timed> fixes) {
        this.fixes = fixes;
    }

    protected NavigableSet<FixType> getInternalRawFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }
    
    /**
     * @return the smoothened fixes; this implementation simply delegates to {@link #getFixes()} because for only
     *         {@link Timed} fixes we can't know how to remove outliers. Subclasses that constrain the
     *         <code>FixType</code> may provide smoothening implementations.
     */
    protected NavigableSet<FixType> getInternalFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }

    /**
     * Iterates the fixes with outliers getting skipped, in the order of their time points.
     * Relies on {@link #getInternalFixes()} to void the track view from outliers.
     */
    @Override
    public NavigableSet<FixType> getFixes() {
        return new UnmodifiableNavigableSet<FixType>(getInternalFixes());
    }

    /**
     * Iterates over the raw sequence of fixes, all potential outliers included
     */
    @Override
    public NavigableSet<FixType> getRawFixes() {
        return new UnmodifiableNavigableSet<FixType>(getInternalRawFixes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) getInternalFixes().floor((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastRawFixAtOrBefore(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().floor((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstRawFixAtOrAfter(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().ceiling((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().ceiling((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastRawFixBefore(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().lower((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().higher((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstRawFixAfter(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().higher((FixType) new DummyTimed(timePoint));
    }

    @Override
    public FixType getFirstRawFix() {
        if (getInternalFixes().isEmpty()) {
            return null;
        } else {
            return (FixType) getInternalFixes().first();
        }
    }
    
    @Override
    public FixType getLastRawFix() {
        if (getInternalRawFixes().isEmpty()) {
            return null;
        } else {
            return (FixType) getInternalRawFixes().last();
        }
    }
    
    @Override
    public Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive) {
        @SuppressWarnings("unchecked")
        Iterator<FixType> result = (Iterator<FixType>) getInternalFixes().tailSet(
                (FixType) new DummyTimed(startingAt), inclusive).iterator();
        return result;
    }

    @Override
    public Iterator<FixType> getRawFixesIterator(TimePoint startingAt, boolean inclusive) {
        @SuppressWarnings("unchecked")
        Iterator<FixType> result = (Iterator<FixType>) getInternalRawFixes().tailSet(
                (FixType) new DummyTimed(startingAt), inclusive).iterator();
        return result;
    }

}
