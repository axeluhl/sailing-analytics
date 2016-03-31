package com.sap.sailing.domain.racelogtracking.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class RegattaLogSensorDataTracker {

    private final ConcurrentHashMap<RegattaAndRaceIdentifier, DynamicTrackedRace> knownTrackedRaces = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RegattaAndRaceIdentifier, RaceLogSensorDataTracker> dataTrackers = new ConcurrentHashMap<>();

    private final TrackedRegatta trackedRegatta;
    private final RaceListener raceListener;

    public RegattaLogSensorDataTracker(TrackedRegatta trackedRegatta) {
        this.trackedRegatta = trackedRegatta;
        raceListener = new RaceListener() {
            @Override
            public void raceRemoved(TrackedRace trackedRace) {
                removeRaceLogSensorDataTracker(trackedRace.getRaceIdentifier());
            }
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                if (trackedRace instanceof DynamicTrackedRace) {
                    DynamicTrackedRace dynamicTrackedRace = (DynamicTrackedRace) trackedRace;
                    RegattaAndRaceIdentifier raceIdentifier = dynamicTrackedRace.getRaceIdentifier();
                    DynamicTrackedRace existingRace = knownTrackedRaces.putIfAbsent(raceIdentifier, dynamicTrackedRace);
                    if (existingRace != null) {
                        removeRaceLogSensorDataTracker(raceIdentifier);
                    } else {
                        RaceLogSensorDataTracker dataTracker = new RaceLogSensorDataTracker(
                                (DynamicTrackedRace) trackedRace);
                        dataTrackers.put(raceIdentifier, dataTracker);
                    }
                }
            }
        };
        
        trackedRegatta.addRaceListener(raceListener);
        
    }

    public void stop() {
        trackedRegatta.removeRaceListener(raceListener);
        knownTrackedRaces.keySet().forEach(raceIdentifier -> {
            removeRaceLogSensorDataTracker(raceIdentifier);
        });
        knownTrackedRaces.clear();
        dataTrackers.clear();
    }

    private void removeRaceLogSensorDataTracker(RegattaAndRaceIdentifier raceIdentifier) {
        RaceLogSensorDataTracker currentActiveDataTracker = dataTrackers.get(raceIdentifier);
        if (currentActiveDataTracker != null) {
            currentActiveDataTracker.stop();
            dataTrackers.remove(currentActiveDataTracker);
        }
    }

}
