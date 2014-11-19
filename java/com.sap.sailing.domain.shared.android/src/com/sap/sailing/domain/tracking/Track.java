package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;

import com.sap.sailing.domain.base.Timed;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * A track records {@link Timed} items for an object of type <code>ItemType</code>. It allows clients to ask for a value
 * close to a given {@link TimePoint}. The track manages a time-based set of raw fixes. An implementation may have an
 * understanding of how to eliminate outliers. For example, if a track implementation knows it's tracking boats, it may
 * consider fixes that the boat cannot possibly have reached due to its speed and direction change limitations as
 * outliers. The set of fixes with outliers filtered out can be obtained using {@link #getFixes} whereas
 * {@link #getRawFixes()} returns the unfiltered, raw fixes. If an implementation has no idea what an outlier is,
 * both methods will return the same fix sequence.<p>
 * 
 * With tracks, concurrency is an important issue. Threads may want to modify a track while other threads may want to
 * read from it. Several methods such as {@link #getLastFixAtOrBefore(TimePoint)} return a single fix and can manage
 * concurrency internally. However, those methods returning a collection of fixes, such as {@link #getFixes()} or an
 * iterator over a collection of fixes, such as {@link #getFixesIterator(TimePoint, boolean)}, need special treatment.
 * Until we internalize such iterations (see bug 824, http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=824),
 * callers need to manage a read lock which is part of a {@link ReadWriteLock} managed by this track. Callers do so
 * by calling {@link #lockForRead} and {@link #unlockAfterRead}.
 * 
 * @author Axel Uhl (d043530)
 */
public interface Track<FixType extends Timed> extends Serializable {
    /**
     * Locks this track for reading by the calling thread. If the thread already holds the lock for this track,
     * the hold count will be incremented. Make sure to call {@link #unlockAfterRead()} in a <code>finally</code>
     * block to release the lock under all possible circumstances. Failure to do so will inevitably lead to
     * deadlocks!
     */
    void lockForRead();
    
    /**
     * Decrements the hold count for this track's read lock for the calling thread. If it goes to zero, the lock will be
     * released and other readers or a writer can obtain the lock. Make sure to call this method in a
     * <code>finally</code> block for each {@link #lockForRead()} invocation.
     */
    void unlockAfterRead();
    
    /**
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     * 
     * @return the smoothened fixes
     */
    Iterable<FixType> getFixes();

    /**
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     * 
     * @return The smoothened fixes between from and to.
     */
    Iterable<FixType> getFixes(TimePoint from, boolean fromInclusive, TimePoint to, boolean toInclusive);

    /**
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     */
    Iterable<FixType> getRawFixes();

    FixType getLastFixAtOrBefore(TimePoint timePoint);

    FixType getLastFixBefore(TimePoint timePoint);

    FixType getLastRawFixAtOrBefore(TimePoint timePoint);

    FixType getFirstFixAtOrAfter(TimePoint timePoint);

    FixType getFirstRawFixAtOrAfter(TimePoint timePoint);

    FixType getLastRawFixBefore(TimePoint timePoint);

    FixType getFirstRawFixAfter(TimePoint timePoint);
    
    FixType getFirstFixAfter(TimePoint timePoint);
    
    /**
     * The first fix in this track or <code>null</code> if the track is empty. The fix returned may
     * be an outlier that is not returned by calls operating on the smoothened version of the track.
     */
    FixType getFirstRawFix();
    
    /**
     * The last fix in this track or <code>null</code> if the track is empty. The fix returned may
     * be an outlier that is not returned by calls operating on the smoothened version of the track.
     */
    FixType getLastRawFix();
    
    /**
     * Returns an iterator starting at the first fix after <code>startingAt</code> (or "at or after" in case
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the smoothened fixes (see
     * also {@link #getFixes()}, without any smoothening or dampening applied.
     * 
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     */
    Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive);

    /**
     * Returns an iterator starting at the first raw fix after <code>startingAt</code> (or "at or after" in case
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the raw fixes (see also
     * {@link #getRawFixes()}, without any smoothening or dampening applied.
     * 
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     */
    Iterator<FixType> getRawFixesIterator(TimePoint startingAt, boolean inclusive);

    /**
     * Returns a descending iterator starting at the first fix before <code>startingAt</code> (or "at or before" in case
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the smoothened fixes (see
     * also {@link #getFixes()}, without any smoothening or dampening applied.
     * 
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     */
    Iterator<FixType> getFixesDescendingIterator(TimePoint startingAt, boolean inclusive);

    /**
     * Returns a descending iterator starting at the first raw fix before <code>startingAt</code> (or "at or before" in case
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the raw fixes (see also
     * {@link #getRawFixes()}, without any smoothening or dampening applied.
     * 
     * Callers must have called {@link #lockForRead()} before calling this method. This will be checked, and an exception
     * will be thrown in case the caller has failed to do so.
     */
    Iterator<FixType> getRawFixesDescendingIterator(TimePoint startingAt, boolean inclusive);
    
    /**
     * @return the average duration between two fixes (outliers removed) in this track or <code>null</code> if there is not
     * more than one fix in the track
     */
    Duration getAverageIntervalBetweenFixes();
    
    /**
     * @return the average duration between two fixes (outliers <em>not</em> removed) in this track or <code>null</code> if there is not
     * more than one raw fix in the track
     */
    Duration getAverageIntervalBetweenRawFixes();
}
