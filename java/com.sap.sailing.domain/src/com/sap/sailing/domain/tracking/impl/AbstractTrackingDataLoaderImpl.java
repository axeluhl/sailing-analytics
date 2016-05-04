package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackingDataLoader;

public abstract class AbstractTrackingDataLoaderImpl implements TrackingDataLoader {
    
    private final DynamicTrackedRace trackedRace;
    private TrackedRaceStatus status;
    
    protected AbstractTrackingDataLoaderImpl(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }
    
    protected void updateStatus(TrackedRaceStatus status) {
        this.status = status;
        this.trackedRace.setStatus(this, status);
    }

    @Override
    public TrackedRaceStatus getStatus() {
        return status;
    }

}
