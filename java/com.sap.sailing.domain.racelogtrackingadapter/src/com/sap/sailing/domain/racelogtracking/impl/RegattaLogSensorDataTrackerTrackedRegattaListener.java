package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;

public class RegattaLogSensorDataTrackerTrackedRegattaListener implements TrackedRegattaListener {

    private final ConcurrentHashMap<Serializable, TrackedRegatta> knownRegattas = new ConcurrentHashMap<Serializable, TrackedRegatta>();
    private final ConcurrentHashMap<Serializable, RegattaLogSensorDataTracker> registeredTrackers = new ConcurrentHashMap<Serializable, RegattaLogSensorDataTracker>();

    @Override
    public void regattaAdded(TrackedRegatta trackedRegatta) {
        Serializable regattaId = trackedRegatta.getRegatta().getId();
        if (knownRegattas.putIfAbsent(regattaId, trackedRegatta) == null) {
            synchronized (knownRegattas) {
                RegattaLogSensorDataTracker tracker = null;
                try {
                    tracker = new RegattaLogSensorDataTracker(trackedRegatta);
                    registeredTrackers.put(regattaId, tracker);
                } finally {
                    if (tracker != null) {
                        tracker.stop();
                    }
                }
            }
        }
    }

    @Override
    public void regattaRemoved(TrackedRegatta trackedRegatta) {
        synchronized (knownRegattas) {
            Serializable regattaId = trackedRegatta.getRegatta().getId();
            try {
                RegattaLogSensorDataTracker tracker = registeredTrackers.get(trackedRegatta.getRegatta().getId());
                tracker.stop();
            } finally {
                knownRegattas.remove(regattaId);
                registeredTrackers.remove(regattaId);
            }
        }
    }
}
