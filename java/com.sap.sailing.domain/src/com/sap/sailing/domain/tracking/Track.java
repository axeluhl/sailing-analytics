package com.sap.sailing.domain.tracking;

import java.util.Iterator;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Timed;

/**
 * A track records {@link Timed} items for an object of type <code>ItemType</code>. It allows clients to ask for a
 * value close to a given {@link TimePoint}.
 * 
 * @author Axel Uhl (d043530)
 */
public interface Track<FixType extends Timed> {
    /**
     * @return the raw fixes as recorded by this track; in particular, no smoothening or dampening of any kind is
     *         applied to the fixes returned by this method.
     */
    Iterable<FixType> getFixes();
    
    Iterable<FixType> getRawFixes();

    FixType getLastFixAtOrBefore(TimePoint timePoint);

    FixType getFirstRawFixAtOrAfter(TimePoint timePoint);

    FixType getLastRawFixBefore(TimePoint timePoint);

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
     * <code>inclusive</code> is <code>true</code>). The fixes returned by the iterator are the raw fixes (see also
     * {@link #getFixes()}, without any smoothening or dampening applied.
     */
    Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive);
}
