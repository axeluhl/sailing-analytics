package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Track;

public class TrackImpl<FixType extends Timed> implements Track<FixType> {
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
    }
    
    public TrackImpl() {
        super();
        this.fixes = new TreeSet<Timed>(TimedComparator.INSTANCE);
    }
    
    protected NavigableSet<FixType> getInternalFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }

    /**
     * Iterates the fixes in the order of their time points
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterable<FixType> getFixes() {
        return (Iterable<FixType>) Collections.unmodifiableSet(fixes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) fixes.floor(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) fixes.ceiling(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixBefore(TimePoint timePoint) {
        return (FixType) fixes.lower(new DummyTimed(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) fixes.higher(new DummyTimed(timePoint));
    }

    @Override
    public Iterator<FixType> getFixes(TimePoint startingAt, boolean inclusive) {
        @SuppressWarnings("unchecked")
        Iterator<FixType> result = (Iterator<FixType>) fixes.tailSet(
                new DummyTimed(startingAt), inclusive).iterator();
        return result;
    }

}
