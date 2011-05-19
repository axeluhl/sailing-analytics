package com.sap.sailing.domain.tracking;

/**
 * An extensible track that can still grow by adding more GPS fixes to it.
 * 
 * @author Axel Uhl (d043530)
 */
public interface DynamicTrack<ItemType, FixType extends GPSFix> extends
        Track<ItemType, FixType> {
    void addGPSFix(FixType gpsFix);

    /**
     * A listener is notified whenever a new fix is added to this track
     */
    void addListener(RawListener<ItemType, FixType> listener);
}
