package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;

class WindListenerSendingToTrackedRace implements IgtimiWindListener {
    private final Iterable<DynamicTrackedRace> trackedRaces;
    private final IgtimiWindTrackerFactory windTrackerFactory;
    private final Map<TrackedRace, Integer> numberOfFixesAppliedPerTrackedRace;

    WindListenerSendingToTrackedRace(Iterable<DynamicTrackedRace> trackedRaces, IgtimiWindTrackerFactory windTrackerFactory) {
        this.trackedRaces = trackedRaces;
        this.windTrackerFactory = windTrackerFactory;
        numberOfFixesAppliedPerTrackedRace = new HashMap<>();
        for (TrackedRace trackedRace : trackedRaces) {
            numberOfFixesAppliedPerTrackedRace.put(trackedRace, 0);
        }
    }

    @Override
    public void windDataReceived(Wind wind, String deviceSerialNumber) {
        for (DynamicTrackedRace trackedRace : trackedRaces) {
            if (trackedRace.recordWind(wind, windTrackerFactory.getWindSource(deviceSerialNumber))) {
                numberOfFixesAppliedPerTrackedRace.put(trackedRace, numberOfFixesAppliedPerTrackedRace.get(trackedRace)+1);
            }
        }
    }

    public Map<TrackedRace, Integer> getFixesAppliedPerTrackedRace() {
        return numberOfFixesAppliedPerTrackedRace;
    }
}