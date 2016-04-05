package com.sap.sailing.domain.racelogtracking.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RegattaLogSensorDataTracker {

    private final ConcurrentHashMap<RegattaAndRaceIdentifier, DynamicTrackedRace> knownTrackedRaces = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RegattaAndRaceIdentifier, RaceLogSensorDataTracker> dataTrackers = new ConcurrentHashMap<>();

    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceListener raceListener;
    private final SensorFixStore sensorFixStore;

    public RegattaLogSensorDataTracker(final DynamicTrackedRegatta trackedRegatta, SensorFixStore sensorFixStore) {
        this.trackedRegatta = trackedRegatta;
        this.sensorFixStore = sensorFixStore;
        raceListener = new RaceListener() {
            @Override
            public void raceRemoved(TrackedRace trackedRace) {
                RegattaLogSensorDataTracker.this.raceRemoved(trackedRace);
            }
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                RegattaLogSensorDataTracker.this.raceAdded(trackedRace);
            }
        };
        
        trackedRegatta.addRaceListener(raceListener);
    }
    
    public void raceRemoved(TrackedRace trackedRace) {
        removeRaceLogSensorDataTracker(trackedRace.getRaceIdentifier());
    }
    
    public void raceAdded(TrackedRace trackedRace) {
        if (trackedRace instanceof DynamicTrackedRace) {
            DynamicTrackedRace dynamicTrackedRace = (DynamicTrackedRace) trackedRace;
            RegattaAndRaceIdentifier raceIdentifier = dynamicTrackedRace.getRaceIdentifier();
            DynamicTrackedRace existingRace = knownTrackedRaces.putIfAbsent(raceIdentifier, dynamicTrackedRace);
            if (existingRace != null) {
                removeRaceLogSensorDataTracker(raceIdentifier);
            } else {
                RaceLogSensorDataTracker dataTracker = new RaceLogSensorDataTracker(
                        (DynamicTrackedRace) trackedRace, trackedRegatta,
                        sensorFixStore);
                dataTrackers.put(raceIdentifier, dataTracker);
            }
        }
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
