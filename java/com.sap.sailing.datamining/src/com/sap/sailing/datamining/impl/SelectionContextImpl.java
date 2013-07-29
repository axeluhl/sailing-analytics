package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class SelectionContextImpl implements SelectionContext {

    private TrackedRegatta trackedRegatta;
    private TrackedRace trackedRace;

    public SelectionContextImpl(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;
    }

    @Override
    public TrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

}
