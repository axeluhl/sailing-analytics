package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sse.common.WithID;

public interface SensorFixTrack<ItemType extends WithID & Serializable, FixT extends SensorFix> extends Track<FixT> {
    
    Iterable<String> getValueNames();
    
    void addListener(SensorFixTrackListener<ItemType, FixT> listener);
    
    void removeListener(SensorFixTrackListener<ItemType, FixT> listener);
    
    ItemType getTrackedItem();
    
    String getTrackName();

}
