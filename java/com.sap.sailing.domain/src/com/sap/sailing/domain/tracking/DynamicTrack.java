package com.sap.sailing.domain.tracking;

/**
 * An extensible track that can still grow by adding more GPS fixes to it.
 * 
 * @author Axel Uhl (d043530)
 */
public interface DynamicTrack<ItemType, FixType extends GPSFix> extends
        GPSFixTrack<ItemType, FixType> {
    /**
     * Adds the <code>gpsFix</code> (or an object equal to it) to this track. Note: depending on the implementation,
     * it's not the <em>same</em> object actually added to the track but only an equal one. This is particularly
     * important when constructing test cases. Don't expect objects returned by {@link #getFixes()} to be the same
     * as those added; they will only be equal.
     */
    void addGPSFix(FixType gpsFix);

    /**
     * A listener is notified whenever a new fix is added to this track
     */
    void addListener(RaceChangeListener<ItemType> listener);
    
    void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage);

}
