package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.GPSFix;

public interface GPSTrackListener<ItemType, FixType extends GPSFix> extends Serializable {
    void gpsFixReceived(FixType fix, ItemType item, boolean firstFixInTrack);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);
    
    /**
     * Listeners can use this to skip their serialization.
     */
    boolean isTransient();
}
