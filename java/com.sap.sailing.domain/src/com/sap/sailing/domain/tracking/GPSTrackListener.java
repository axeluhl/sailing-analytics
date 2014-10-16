package com.sap.sailing.domain.tracking;

import java.io.Serializable;

public interface GPSTrackListener<ItemType, FixType extends GPSFix> extends Serializable {
    void gpsFixReceived(FixType fix, ItemType item, boolean firstFixInTrack);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);
    
    /**
     * Listeners can use this to skip their serialization.
     */
    boolean isTransient();
}
