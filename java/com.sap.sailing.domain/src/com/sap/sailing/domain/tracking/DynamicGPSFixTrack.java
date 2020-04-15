package com.sap.sailing.domain.tracking;

import java.util.ConcurrentModificationException;

import com.sap.sailing.domain.common.tracking.GPSFix;

/**
 * An extensible track that can still grow by adding more GPS fixes to it. Callers of {@link GPSFixTrack#getFixes()}
 * or {@link #getRawFixes()} or {@link #getFixesIterator(com.sap.sailing.domain.base.TimePoint, boolean)} or
 * {@link #getRawFixesIterator(com.sap.sailing.domain.base.TimePoint, boolean)} must make sure they synchronize
 * on this object before starting an iteration. Otherwise, {@link ConcurrentModificationException} may result.
 * 
 * @author Axel Uhl (d043530)
 */
public interface DynamicGPSFixTrack<ItemType, FixType extends GPSFix> extends GPSFixTrack<ItemType, FixType>, DynamicTrack<FixType> {
    /**
     * Adds the <code>gpsFix</code> (or an object equal to it) to this track. Note: depending on the implementation,
     * it's not the <em>same</em> object actually added to the track but only an equal one. This is particularly
     * important when constructing test cases. Don't expect objects returned by {@link #getFixes()} to be the same
     * as those added; they will only be equal.<p>
     * 
     * Implementations will usually implement this as a <code>synchronized</code> operation that is mutual exclusive
     * to iterating over the fixes of the track or any subset thereof.
     * 
     * @return {@code true} if and only if the fix was actually added to the competitor's track
     */
    boolean addGPSFix(FixType gpsFix);

    void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage);

}
