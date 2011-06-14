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
    Iterable<FixType> getFixes();

    FixType getLastFixAtOrBefore(TimePoint timePoint);

    FixType getFirstFixAtOrAfter(TimePoint timePoint);

    FixType getLastFixBefore(TimePoint timePoint);

    FixType getFirstFixAfter(TimePoint timePoint);
    
    /**
     * The first fix in this track or <code>null</code> if the track is empty
     */
    FixType getFirstFix();
    
    /**
     * The last fix in this track or <code>null</code> if the track is empty
     */
    FixType getLastFix();
    
    /**
     * Returns an iterator starting at the first fix after <code>startingAt</code> (or
     * "at or after" in case <code>inclusive</code> is <code>true</code>).
     */
    Iterator<FixType> getFixesIterator(TimePoint startingAt, boolean inclusive);
}
