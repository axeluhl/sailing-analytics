package com.sap.sailing.domain.racelogtracking.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelogtracking.impl.logtracker.RaceLogSensorFixTrackerLifecycle;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RegattaLogSensorDataTracker {
    private final Map<RegattaAndRaceIdentifier, DynamicTrackedRace> knownTrackedRaces = new ConcurrentHashMap<>();
    private final Map<RegattaAndRaceIdentifier, RaceLogSensorFixTrackerLifecycle> dataTrackers = new ConcurrentHashMap<>();
    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceListener raceListener;
    private final SensorFixStore sensorFixStore;
    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RegattaLogSensorDataTracker(final DynamicTrackedRegatta trackedRegatta, SensorFixStore sensorFixStore,
            final SensorFixMapperFactory sensorFixMapperFactory) {
        this.trackedRegatta = trackedRegatta;
        this.sensorFixStore = sensorFixStore;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.raceListener = new RaceListener() {
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

    public synchronized void raceAdded(TrackedRace trackedRace) {
        if (trackedRace instanceof DynamicTrackedRace) {
            DynamicTrackedRace dynamicTrackedRace = (DynamicTrackedRace) trackedRace;
            RegattaAndRaceIdentifier raceIdentifier = dynamicTrackedRace.getRaceIdentifier();
            DynamicTrackedRace existingRace = knownTrackedRaces.get(raceIdentifier);
            if (existingRace != null) {
                removeRaceLogSensorDataTracker(raceIdentifier);
            }
            RaceLogSensorFixTrackerLifecycle dataTracker = new RaceLogSensorFixTrackerLifecycle((DynamicTrackedRace) trackedRace,
                    sensorFixStore, sensorFixMapperFactory, tracker -> trackerStopped(raceIdentifier, tracker));
            dataTrackers.put(raceIdentifier, dataTracker);
        }
    }

    private void trackerStopped(RegattaAndRaceIdentifier raceIdentifier, RaceLogSensorFixTrackerLifecycle tracker) {
        dataTrackers.remove(raceIdentifier, tracker);
    }

    public synchronized void stop() {
        trackedRegatta.removeRaceListener(raceListener);
        knownTrackedRaces.keySet().forEach(this::removeRaceLogSensorDataTracker);
        knownTrackedRaces.clear();
        dataTrackers.clear();
    }

    private void removeRaceLogSensorDataTracker(RegattaAndRaceIdentifier raceIdentifier) {
        RaceLogSensorFixTrackerLifecycle currentActiveDataTracker = dataTrackers.get(raceIdentifier);
        if (currentActiveDataTracker != null) {
            currentActiveDataTracker.stop();
            trackerStopped(raceIdentifier, currentActiveDataTracker);
        }
    }

    @Override
    public String toString() {
        return "RegattaLogSensorDataTracker [regattaId=" + trackedRegatta.getRegatta().getId() + "]";
    }
    
}
