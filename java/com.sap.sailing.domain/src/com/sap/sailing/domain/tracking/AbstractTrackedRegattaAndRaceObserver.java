package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;

/**
 * This listener is informed about every {@link TrackedRegatta} by its implemented {@link TrackedRegattaListener}. To
 * make this work as intended, subclasses need to be registered at the OSGi service registry by exporting
 * {@link TrackedRegattaListener}.<br>
 * For every known {@link TrackedRegatta}, added and removed TrackedRaces are observed by a RaceListener. Subclasses
 * need to implement {@link #onRaceAdded(RegattaAndRaceIdentifier, DynamicTrackedRegatta, DynamicTrackedRace)} and
 * {@link #onRaceRemoved(DynamicTrackedRace)} to further process {@link TrackedRace}s.
 */
public abstract class AbstractTrackedRegattaAndRaceObserver implements TrackedRegattaListener {

    private static final Logger log = Logger.getLogger(AbstractTrackedRegattaAndRaceObserver.class.getName());

    private final Map<Serializable, RegattaListener> registeredRegattaListeners;

    public AbstractTrackedRegattaAndRaceObserver() {
        registeredRegattaListeners = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void regattaAdded(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        synchronized (registeredRegattaListeners) {
            this.stopIfNotNull(registeredRegattaListeners.remove(regattaId));
            RegattaListener tracker = new RegattaListener((DynamicTrackedRegatta) trackedRegatta);
            registeredRegattaListeners.put(regattaId, tracker);
        }
        log.fine("Added sensor data tracker to tracked regatta: " + trackedRegatta.getRegatta().getName());
    }

    @Override
    public final synchronized void regattaRemoved(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        synchronized (registeredRegattaListeners) {
            try {
                this.stopIfNotNull(registeredRegattaListeners.get(regattaId));
            } finally {
                registeredRegattaListeners.remove(regattaId);
            }
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
    
    protected synchronized void removeAll() {
        synchronized (registeredRegattaListeners) {
            registeredRegattaListeners.values().forEach(this::stopIfNotNull);
        }
    }

    /**
     * The method is called when a {@link TrackedRace} has been added to a {@link TrackedRegatta} that was
     * {@link #regattaAdded(TrackedRegatta) added} to this listener.<p>
     * 
     * The method is called {@code synchronized} under the {@link RegattaListener}'s monitor.
     */
    protected abstract void onRaceAdded(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRegatta trackedRegatta,
            DynamicTrackedRace trackedRace);

    /**
     * The method is called when a {@link TrackedRace} has been removed from a {@link TrackedRegatta} that was
     * {@link #regattaRemoved(TrackedRegatta) removed} from this listener.<p>
     * 
     * The method is called {@code synchronized} under the {@link RegattaListener}'s monitor.
     */
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
            trackedRegatta.addRaceListener(raceListener, /* Not replicated */ Optional.empty(), /* synchronous */ false);
        }

        public synchronized void raceRemoved(TrackedRace trackedRace) {
            remove(trackedRace.getRaceIdentifier());
        }

        public synchronized void raceAdded(TrackedRace trackedRace) {
            if (trackedRace instanceof DynamicTrackedRace) {
                DynamicTrackedRace dynamicTrackedRace = (DynamicTrackedRace) trackedRace;
                RegattaAndRaceIdentifier raceIdentifier = dynamicTrackedRace.getRaceIdentifier();
                if (knownTrackedRaces.containsKey(raceIdentifier)) {
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
         * Called by {@link AbstractTrackedRegattaAndRaceObserver} when the {@link TrackedRegatta} was removed or the
         * {@link TrackedRegatta} shouldn't be tracked anymore (this is e.g. the case in replication state).
         */
        public void stop() {
            try {
                // It's necessary that we wait for the returned future to be completed
                // to ensure that all regular removals are correctly processed before
                // manually removing the left over ones (see below).
                // This is now necessary due to the fact that TrackedRegattaImpl fires events concurrently,
                // which makes it possible to receive the removal of the TrackedRegatta before the removals
                // for the contained TrackedRaces are received.
                trackedRegatta.removeRaceListener(raceListener).get();
            } catch (InterruptedException | ExecutionException e) {
                log.log(Level.SEVERE, "Error while waiting for the raceListener to be removed", e);
            }
            // Depending on the code path (e.g. replication) it's possible that we still track some TrackedRace at this point.
            // We need to ensure that all listeners are correctly stopped to not leak any instances.
            synchronized (this) {
                knownTrackedRaces.keySet().forEach(this::remove);
                knownTrackedRaces.clear();
            }
        }

        @Override
        public String toString() {
            return "TrackedRegattaAndRaceObserver.RegattaListener [regattaId=" + trackedRegatta.getRegatta().getId()
                    + "]";
        }
    }
}
