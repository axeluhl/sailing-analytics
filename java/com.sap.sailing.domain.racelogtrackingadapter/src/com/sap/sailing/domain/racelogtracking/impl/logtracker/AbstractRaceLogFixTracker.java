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

    // TEMP: usage in RaceLogRaceTracker überprüfen
    public DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    public final DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getStartOfTracking() {
        return trackedRace.getStartOfTracking();
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getEndOfTracking() {
        return trackedRace.getEndOfTracking();
    }

    // TEMP: kommt von RaceLogRaceTracker
    public void updateStartAndEndOfTracking() {
        trackedRace.updateStartAndEndOfTracking();
    }
}
