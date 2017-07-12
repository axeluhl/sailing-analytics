package com.sap.sailing.server.statistics;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sailing.server.RacingEventService;

/**
 * This listener is informed about every {@link TrackedRegatta} by {@link RacingEventService} via the implemented
 * {@link TrackedRegattaListener}. For every known {@link TrackedRegatta}, added and removed TrackedRaces are observed
 * by a RaceListener. Subclasses need to implement
 * {@link #onRaceAdded(RegattaAndRaceIdentifier, DynamicTrackedRegatta, DynamicTrackedRace)} and
 * {@link #onRaceRemoved(DynamicTrackedRace)} to further process {@link TrackedRace}s.
 */
public abstract class TrackedRegattaAndRaceObserver implements TrackedRegattaListener {

    private static final Logger log = Logger.getLogger(TrackedRegattaAndRaceObserver.class.getName());

    private final Map<Serializable, RegattaListener> registeredRegattaListeners = new ConcurrentHashMap<>();

    public TrackedRegattaAndRaceObserver() {
    }

    @Override
    public final synchronized void regattaAdded(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        RegattaListener tracker = new RegattaListener((DynamicTrackedRegatta) trackedRegatta);
        this.stopIfNotNull(registeredRegattaListeners.put(regattaId, tracker));
        log.fine("Added sensor data tracker to tracked regatta: " + trackedRegatta.getRegatta().getName());
    }

    @Override
    public final synchronized void regattaRemoved(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        try {
            this.stopIfNotNull(registeredRegattaListeners.get(regattaId));
        } finally {
            registeredRegattaListeners.remove(regattaId);
        }
    }

    private void stopIfNotNull(RegattaListener tracker) {
        if (tracker != null) {
            try {
                tracker.stop();
            } catch (Exception exc) {
                log.log(Level.SEVERE, "Stopping of tracker failed: " + tracker, exc);
            }
        }
    }

    protected abstract void onRaceAdded(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRegatta trackedRegatta,
            DynamicTrackedRace trackedRace);

    protected abstract void onRaceRemoved(DynamicTrackedRace trackedRace);

    private class RegattaListener {
        private final Map<RegattaAndRaceIdentifier, DynamicTrackedRace> knownTrackedRaces = new ConcurrentHashMap<>();
        private final DynamicTrackedRegatta trackedRegatta;
        private final RaceListener raceListener;

        public RegattaListener(final DynamicTrackedRegatta trackedRegatta) {
            this.trackedRegatta = trackedRegatta;
            this.raceListener = new RaceListener() {
                @Override
                public void raceRemoved(TrackedRace trackedRace) {
                    RegattaListener.this.raceRemoved(trackedRace);
                }

                @Override
                public void raceAdded(TrackedRace trackedRace) {
                    RegattaListener.this.raceAdded(trackedRace);
                }
            };
            trackedRegatta.addRaceListener(raceListener);
        }

        public void raceRemoved(TrackedRace trackedRace) {
            remove(trackedRace.getRaceIdentifier());
        }

        public synchronized void raceAdded(TrackedRace trackedRace) {
            if (trackedRace instanceof DynamicTrackedRace) {
                DynamicTrackedRace dynamicTrackedRace = (DynamicTrackedRace) trackedRace;
                RegattaAndRaceIdentifier raceIdentifier = dynamicTrackedRace.getRaceIdentifier();
                DynamicTrackedRace existingRace = knownTrackedRaces.get(raceIdentifier);
                if (existingRace != null) {
                    remove(raceIdentifier);
                }
                knownTrackedRaces.put(raceIdentifier, dynamicTrackedRace);
                onRaceAdded(raceIdentifier, trackedRegatta, dynamicTrackedRace);
            }
        }

        private void remove(RegattaAndRaceIdentifier raceIdentifier) {
            DynamicTrackedRace removedTrackedRace = knownTrackedRaces.remove(raceIdentifier);
            onRaceRemoved(removedTrackedRace);
        }

        /**
         * Called by {@link TrackedRegattaAndRaceObserver} when the {@link TrackedRegatta} was removed or the
         * {@link TrackedRegatta} shouldn't be tracked anymore (this is e.g. the case in replication state.
         */
        public synchronized void stop() {
            trackedRegatta.removeRaceListener(raceListener);
            knownTrackedRaces.keySet().forEach(this::remove);
            knownTrackedRaces.clear();
        }

        @Override
        public String toString() {
            return "TrackedRegattaAndRaceObserver.RegattaListener [regattaId=" + trackedRegatta.getRegatta().getId()
                    + "]";
        }
    }
}
