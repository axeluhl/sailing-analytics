package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;

public class RaceLogGPSFixTracker extends AbstractRaceLogFixTracker {
    public RaceLogGPSFixTracker(DynamicTrackedRegatta regatta, DynamicTrackedRace trackedRace) {
        super(regatta, trackedRace);
    }
}
