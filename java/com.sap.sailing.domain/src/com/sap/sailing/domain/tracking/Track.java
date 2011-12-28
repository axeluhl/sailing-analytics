package com.sap.sailing.domain.tracking;

import java.util.Iterator;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Timed;

/**
 * A track records {@link Timed} items for an object of type <code>ItemType</code>. It allows clients to ask for a value
 * close to a given {@link TimePoint}. The track manages a time-based set of raw fixes. An implementation may have an
 * understanding of how to eliminate outliers. For example, if a track implementation knows it's tracking boats, it may
 * consider fixes that the boat cannot possibly have reached due to its speed and direction change limitations as
 * outliers. The set of fixes with outliers filtered out can be obtained using {@link #getFixes} whereas
 * {@link #getRawFixes()} returns the unfilteres, raw fixes. If an implementation has no idea what an outlier is,
 * both methods will return the same fix sequence.
 * 
 * @author Axel Uhl (d043530)
 */
public interface Track<FixType extends Timed> {
    /**
     * Callers must synchronize on this object before iterating the result if they have to expect concurrent
     * modifications.
     * 
     * @return the raw fixes as recorded by this track; in particular, no smoothening or dampening of any kind is
     *         applied to the fixes returned by this method.
     */
    Iterable<FixType> getFixes();

    /**
     * Callers must synchronize on this object before iterating the result if they have to expect concurrent
     * modifications.
     */
    Iterable<FixType> getRawFixes();

    FixType getLastFixAtOrBefore(TimePoint timePoint);

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
     * Callers must synchronize on this object before iterating the result if they have to expect concurrent
     * modifications.
     */
    Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive);

    /**
     * Returns an iterator starting at the first raw fix after <code>startingAt</code> (or "at or after" in case
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the raw fixes (see also
     * {@link #getRawFixes()}, without any smoothening or dampening applied.
     * 
     * Callers must synchronize on this object before iterating the result if they have to expect concurrent
     * modifications.
     */
    Iterator<FixType> getRawFixesIterator(TimePoint startingAt, boolean inclusive);
}
