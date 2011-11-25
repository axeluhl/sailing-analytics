package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

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
     *         {@link Timed} fixes we can't know how to smoothen anything. Subclasses that constrain the
     *         <code>FixType</code> may provide smoothening implementations.
     */
    protected NavigableSet<FixType> getInternalFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }

    /**
     * Iterates the fixes with outliers getting skipped, in the order of their time points.
     * Relies on {@link #getInternalFixes()} to smoothen the track.
     */
    @Override
    public Iterable<FixType> getFixes() {
        return (Iterable<FixType>) Collections.unmodifiableSet(getInternalFixes());
    }

    /**
     * Iterates over the raw sequence of fixes, all potential outliers included
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterable<FixType> getRawFixes() {
        return (Iterable<FixType>) Collections.unmodifiableSet(fixes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) getInternalFixes().floor((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastRawFixAtOrBefore(TimePoint timePoint) {
        return (FixType) fixes.floor(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstRawFixAtOrAfter(TimePoint timePoint) {
        return (FixType) fixes.ceiling(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().ceiling((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastRawFixBefore(TimePoint timePoint) {
        return (FixType) fixes.lower(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().higher((FixType) new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstRawFixAfter(TimePoint timePoint) {
        return (FixType) fixes.higher(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstRawFix() {
        if (fixes.isEmpty()) {
            return null;
        } else {
            return (FixType) fixes.first();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastRawFix() {
        if (fixes.isEmpty()) {
            return null;
        } else {
            return (FixType) fixes.last();
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
        Iterator<FixType> result = (Iterator<FixType>) fixes.tailSet(
                new DummyTimed(startingAt), inclusive).iterator();
        return result;
    }

}
