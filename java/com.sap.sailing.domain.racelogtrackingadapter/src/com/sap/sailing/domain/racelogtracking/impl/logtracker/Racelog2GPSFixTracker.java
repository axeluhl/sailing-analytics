package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;

public class Racelog2GPSFixTracker extends AbstractRaceLogFixTracker {
    public Racelog2GPSFixTracker(DynamicTrackedRegatta regatta, DynamicTrackedRace trackedRace) {
        super(regatta, trackedRace);
    }
}
