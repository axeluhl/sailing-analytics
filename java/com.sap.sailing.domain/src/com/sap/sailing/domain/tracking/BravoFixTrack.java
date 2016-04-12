package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public interface BravoFixTrack<ItemType extends WithID & Serializable> extends SensorFixTrack<ItemType, BravoFix> {
    public static final String TRACK_NAME = "BravoFixTrack";
    
    Double getRideHeight(TimePoint timePoint);
    
    Double getAverageRideHeight(TimePoint from, TimePoint to);
}
