package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;

class WindListenerSendingToTrackedRace implements IgtimiWindListener {
    private final Iterable<DynamicTrackedRace> trackedRaces;
    private final IgtimiWindTrackerFactory windTrackerFactory;

    WindListenerSendingToTrackedRace(Iterable<DynamicTrackedRace> trackedRaces, IgtimiWindTrackerFactory windTrackerFactory) {
        this.trackedRaces = trackedRaces;
        this.windTrackerFactory = windTrackerFactory;
    }

    @Override
    public void windDataReceived(Wind wind, String deviceSerialNumber) {
        for (DynamicTrackedRace trackedRace : trackedRaces) {
            trackedRace.recordWind(wind, windTrackerFactory.getWindSource(deviceSerialNumber));
        }
    }
}