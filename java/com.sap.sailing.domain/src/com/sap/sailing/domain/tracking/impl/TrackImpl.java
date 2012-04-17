package com.sap.sailing.domain.tracking.impl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.util.impl.ArrayListNavigableSet;
import com.sap.sailing.util.impl.UnmodifiableNavigableSet;

public abstract class TrackImpl<FixType extends Timed> implements Track<FixType> {
    /**
     * The fixes, ordered by their time points
     */
    private final NavigableSet<Timed> fixes;
    
    protected static class DummyTimed implements Timed {
        private static final long serialVersionUID = 6047311973718918856L;
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

    /**
     * Callers that want to iterate over the collection returned need to synchronize on <code>this</code> object to avoid
     * {@link ConcurrentModificationException}s.
     */
    protected NavigableSet<FixType> getInternalRawFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }
    
    /**
     * Callers that want to iterate over the collection returned need to synchronize on <code>this</code> object to
     * avoid {@link ConcurrentModificationException}s.
     * 
     * @return the smoothened fixes; this implementation simply delegates to {@link #getInternalRawFixes()} because for
     *         only {@link Timed} fixes we can't know how to remove outliers. Subclasses that constrain the
     *         <code>FixType</code> may provide smoothening implementations.
     */
    protected NavigableSet<FixType> getInternalFixes() {
        NavigableSet<FixType> result = getInternalRawFixes();
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

    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) getInternalFixes().floor(getDummyFix(timePoint));
    }
    
    @Override
    public FixType getLastFixBefore(TimePoint timePoint) {
        return (FixType) getInternalFixes().lower(getDummyFix(timePoint));
    }

    @Override
    public FixType getLastRawFixAtOrBefore(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().floor(getDummyFix(timePoint));
    }

    @Override
    public FixType getFirstRawFixAtOrAfter(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().ceiling(getDummyFix(timePoint));
    }

    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().ceiling(getDummyFix(timePoint));
    }

    @Override
    public FixType getLastRawFixBefore(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().lower(getDummyFix(timePoint));
    }

    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) getInternalFixes().higher(getDummyFix(timePoint));
    }

    @Override
    public FixType getFirstRawFixAfter(TimePoint timePoint) {
        return (FixType) getInternalRawFixes().higher(getDummyFix(timePoint));
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
        Iterator<FixType> result = (Iterator<FixType>) getInternalFixes().tailSet(
                getDummyFix(startingAt), inclusive).iterator();
        return result;
    }

    /**
     * Creates a dummy fix that conforms to <code>FixType</code>. This in particular means that subclasses
     * instantiating <code>FixType</code> with a specific class need to redefine this method so as to return
     * a dummy fix complying with their instantiation type used for <code>FixType</code>. Otherwise, a
     * {@link ClassCastException} may result upon certain operations performed with the fix returned by
     * this method.
     */
    protected FixType getDummyFix(TimePoint timePoint) {
        @SuppressWarnings("unchecked")
        FixType result = (FixType) new DummyTimed(timePoint);
        return result;
    }

    @Override
    public Iterator<FixType> getRawFixesIterator(TimePoint startingAt, boolean inclusive) {
        Iterator<FixType> result = (Iterator<FixType>) getInternalRawFixes().tailSet(
                getDummyFix(startingAt), inclusive).iterator();
        return result;
    }

}
