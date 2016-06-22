package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

/**
 * An instance of this class is created for every {@link TrackedRegatta} by {@link RegattaLogFixTrackerRegattaListener}.
 * This observes the given {@link TrackedRegatta} to get to know about every {@link TrackedRace} of the
 * {@link TrackedRegatta}.
 * 
 * An instance of {@link RaceLogFixTrackerManager} is created for every {@link TrackedRace}. It is ensured that
 * {@link RaceLogFixTrackerManager} are stopped when a {@link TrackedRace} is removed from the {@link TrackedRegatta}.
 */
public class RegattaLogFixTrackerRaceListener {
    private final Map<RegattaAndRaceIdentifier, DynamicTrackedRace> knownTrackedRaces = new ConcurrentHashMap<>();
    private final Map<RegattaAndRaceIdentifier, RaceLogFixTrackerManager> dataTrackers = new ConcurrentHashMap<>();
    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceListener raceListener;
    private final SensorFixStore sensorFixStore;
    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RegattaLogFixTrackerRaceListener(final DynamicTrackedRegatta trackedRegatta, SensorFixStore sensorFixStore,
            final SensorFixMapperFactory sensorFixMapperFactory) {
        this.trackedRegatta = trackedRegatta;
        this.sensorFixStore = sensorFixStore;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.raceListener = new RaceListener() {
            @Override
            public void raceRemoved(TrackedRace trackedRace) {
                RegattaLogFixTrackerRaceListener.this.raceRemoved(trackedRace);
            }

            @Override
            public void raceAdded(TrackedRace trackedRace) {
                RegattaLogFixTrackerRaceListener.this.raceAdded(trackedRace);
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
            RaceLogFixTrackerManager trackerManager = new RaceLogFixTrackerManager((DynamicTrackedRace) trackedRace,
                    sensorFixStore, sensorFixMapperFactory, tracker -> trackerStopped(raceIdentifier, tracker));
            dataTrackers.put(raceIdentifier, trackerManager);
        }
    }

    private void trackerStopped(RegattaAndRaceIdentifier raceIdentifier, RaceLogFixTrackerManager trackerManager) {
        dataTrackers.remove(raceIdentifier, trackerManager);
    }

    /**
     * Called by {@link RegattaLogFixTrackerRegattaListener} when the {@link TrackedRegatta} was removed or the
     * {@link TrackedRegatta} shouldn't be tracked anymore (this is e.g. the case in replication state.
     */
    public synchronized void stop() {
        trackedRegatta.removeRaceListener(raceListener);
        knownTrackedRaces.keySet().forEach(this::removeRaceLogSensorDataTracker);
        knownTrackedRaces.clear();
        dataTrackers.clear();
    }

    private void removeRaceLogSensorDataTracker(RegattaAndRaceIdentifier raceIdentifier) {
        RaceLogFixTrackerManager currentActiveDataTracker = dataTrackers.get(raceIdentifier);
        if (currentActiveDataTracker != null) {
            currentActiveDataTracker.stop(false);
            trackerStopped(raceIdentifier, currentActiveDataTracker);
        }
    }

    @Override
    public String toString() {
        return "RegattaLogSensorDataTracker [regattaId=" + trackedRegatta.getRegatta().getId() + "]";
    }
    
}
