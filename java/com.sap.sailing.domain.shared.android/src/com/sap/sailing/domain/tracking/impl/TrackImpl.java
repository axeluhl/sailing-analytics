package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.util.impl.ArrayListNavigableSet;
import com.sap.sse.util.impl.UnmodifiableNavigableSet;

public class TrackImpl<FixType extends Timed> implements Track<FixType> {
    private static final long serialVersionUID = -4075853657857657528L;
    /**
     * The fixes, ordered by their time points
     */
    private final ArrayListNavigableSet<Timed> fixes;

    private final NamedReentrantReadWriteLock readWriteLock;

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
    
    public TrackImpl(String nameForReadWriteLock) {
        this(new ArrayListNavigableSet<Timed>(TimedComparator.INSTANCE), nameForReadWriteLock);
    }
    
    protected TrackImpl(ArrayListNavigableSet<Timed> fixes, String nameForReadWriteLock) {
        this.readWriteLock = new NamedReentrantReadWriteLock(nameForReadWriteLock, /* fair */ false);
        this.fixes = fixes;
    }
    
    /**
     * Synchronize the serialization such that no fixes are added while serializing
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        lockForRead();
        try {
            s.defaultWriteObject();
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public void lockForRead() {
        LockUtil.lockForRead(readWriteLock);
    }

    @Override
    public void unlockAfterRead() {
        LockUtil.unlockAfterRead(readWriteLock);
    }
    
    protected void lockForWrite() {
        LockUtil.lockForWrite(readWriteLock);
    }
    
    protected void unlockAfterWrite() {
        LockUtil.unlockAfterWrite(readWriteLock);
    }

    /**
     * Callers that want to iterate over the collection returned need to use {@link #lockForRead()} and {@link #unlockAfterRead()}
     * to avoid {@link ConcurrentModificationException}s. Should they modify the structure returned, they have to use
     * {@link #lockForWrite()} and {@link #unlockAfterWrite()}, respectively.
     */
    protected NavigableSet<FixType> getInternalRawFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<FixType> result = (NavigableSet<FixType>) fixes;
        return result;
    }

    /**
     * asserts that the calling thread holds at least one of read and write lock
     */
    protected void assertReadLock() {
        if (readWriteLock.getReadHoldCount() < 1 && readWriteLock.getWriteHoldCount() < 1) {
            throw new IllegalStateException("Caller must obtain read lock using lockForRead() before calling this method");
        }
    }
    
    protected void assertWriteLock() {
        if (readWriteLock.getWriteHoldCount() < 1) {
            throw new IllegalStateException("Caller must obtain write lock using lockForWrite() before calling this method");
        }
    }

    /**
     * Callers that want to iterate over the collection returned need to synchronize on <code>this</code> object to
     * avoid {@link ConcurrentModificationException}s.
     * 
     * @return the smoothened fixes ordered by their time points; this implementation simply delegates to {@link #getInternalRawFixes()} because for
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
        assertReadLock();
        return new UnmodifiableNavigableSet<FixType>(getInternalFixes());
    }
    
    @Override
    public Iterable<FixType> getFixes(TimePoint from, boolean fromInclusive, TimePoint to, boolean toInclusive) {
        return getFixes().subSet(getDummyFix(from), fromInclusive, getDummyFix(to), toInclusive);
    }

    /**
     * Iterates over the raw sequence of fixes, all potential outliers included
     */
    @Override
    public NavigableSet<FixType> getRawFixes() {
        assertReadLock();
        return new UnmodifiableNavigableSet<FixType>(getInternalRawFixes());
    }

    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalFixes().floor(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public FixType getLastFixBefore(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalFixes().lower(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getLastRawFixAtOrBefore(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalRawFixes().floor(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getFirstRawFixAtOrAfter(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalRawFixes().ceiling(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalFixes().ceiling(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getLastRawFixBefore(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalRawFixes().lower(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalFixes().higher(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getFirstRawFixAfter(TimePoint timePoint) {
        lockForRead();
        try {
            return (FixType) getInternalRawFixes().higher(getDummyFix(timePoint));
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public FixType getFirstRawFix() {
        lockForRead();
        try {
            if (getInternalFixes().isEmpty()) {
                return null;
            } else {
                return (FixType) getInternalFixes().first();
            }
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public FixType getLastRawFix() {
        lockForRead();
        try {
            if (getInternalRawFixes().isEmpty()) {
                return null;
            } else {
                return (FixType) getInternalRawFixes().last();
            }
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive) {
        assertReadLock();
        Iterator<FixType> result = (Iterator<FixType>) getInternalFixes().tailSet(
                getDummyFix(startingAt), inclusive).iterator();
        return result;
    }

    @Override
    public Iterator<FixType> getFixesDescendingIterator(TimePoint startingAt, boolean inclusive) {
        assertReadLock();
        Iterator<FixType> result = (Iterator<FixType>) getInternalFixes().headSet(
                getDummyFix(startingAt), inclusive).descendingIterator();
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
        assertReadLock();
        Iterator<FixType> result = (Iterator<FixType>) getInternalRawFixes().tailSet(
                getDummyFix(startingAt), inclusive).iterator();
        return result;
    }

    @Override
    public Iterator<FixType> getRawFixesDescendingIterator(TimePoint startingAt, boolean inclusive) {
        assertReadLock();
        Iterator<FixType> result = (Iterator<FixType>) getInternalRawFixes().headSet(
                getDummyFix(startingAt), inclusive).descendingIterator();
        return result;
    }

    protected boolean add(FixType fix) {
        lockForWrite();
        try {
            return addWithoutLocking(fix);
        } finally {
            unlockAfterWrite();
        }
    }

    /**
     * The caller must ensure to hold the write lock for this track when calling this methos
     */
    protected boolean addWithoutLocking(FixType fix) {
        return getInternalRawFixes().add(fix);
    }

    @Override
    public Duration getAverageIntervalBetweenFixes() {
        lockForRead();
        try {
            final Duration result;
            final int size = getFixes().size();
            if (size > 1) {
                result = getFixes().first().getTimePoint().until(getFixes().last().getTimePoint()).divide(size-1);
            } else {
                result = null;
            }
            return result;
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Duration getAverageIntervalBetweenRawFixes() {
        lockForRead();
        try {
            final Duration result;
            final int size = getRawFixes().size();
            if (size > 1) {
                result = getRawFixes().first().getTimePoint().until(getRawFixes().last().getTimePoint()).divide(size-1);
            } else {
                result = null;
            }
            return result;
        } finally {
            unlockAfterRead();
        }
    }
}
