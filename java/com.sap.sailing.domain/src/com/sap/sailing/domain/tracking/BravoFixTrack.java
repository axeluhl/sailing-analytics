package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.TimePoint;

public interface BravoFixTrack extends SensorFixTrack<BravoFix> {
    public static final String TRACK_NAME = "BravoFixTrack";
    
    Double getRideHeight(TimePoint timePoint);
}
