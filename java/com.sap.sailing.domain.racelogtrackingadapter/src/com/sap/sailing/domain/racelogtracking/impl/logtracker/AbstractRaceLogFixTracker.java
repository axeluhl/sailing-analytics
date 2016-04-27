package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sse.common.TimePoint;

public class AbstractRaceLogFixTracker {

    private final DynamicTrackedRegatta trackedRegatta;
    private final DynamicTrackedRace trackedRace;

    public AbstractRaceLogFixTracker(DynamicTrackedRegatta trackedRegatta, DynamicTrackedRace trackedRace) {
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;
    }

    public DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    public final DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    protected TimePoint getStartOfTracking() {
        return trackedRace.getStartOfTracking();
    }

    protected TimePoint getEndOfTracking() {
        return trackedRace.getEndOfTracking();
    }

    public void updateStartAndEndOfTracking() {
        trackedRace.updateStartAndEndOfTracking();
    }
}
