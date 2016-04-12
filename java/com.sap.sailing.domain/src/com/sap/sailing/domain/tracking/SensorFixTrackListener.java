package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.tracking.SensorFix;

public interface SensorFixTrackListener<ItemType, FixType extends SensorFix> extends TrackListener {
    
    void fixReceived(FixType fix, ItemType item, boolean firstFixInTrack);

}
