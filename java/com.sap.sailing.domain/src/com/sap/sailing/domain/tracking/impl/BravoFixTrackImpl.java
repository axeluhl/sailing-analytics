package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public class BravoFixTrackImpl extends SensorFixTrackImpl<BravoFix> implements DynamicBravoFixTrack {
    private static final long serialVersionUID = 3045856366552398911L;

    public BravoFixTrackImpl(WithID trackedItem) {
        super(BravoSensorDataMetadata.INSTANCE.getColumns(), BravoFixTrack.TRACK_NAME + " for " + trackedItem);
    }

    @Override
    public Double getRideHeight(TimePoint timePoint) {
        BravoFix fix = getFirstFixAtOrAfter(timePoint);
        if(fix != null) {
            return fix.getRideHeight();
        }
        return null;
    }
}
