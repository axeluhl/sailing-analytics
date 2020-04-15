package com.sap.sailing.server.anniversary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.AbstractTrackedRegattaAndRaceObserver;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;

/**
 * {@link AbstractTrackedRegattaAndRaceObserver} that observes {@link TrackedRace}s to determine when the prerequisites
 * for anniversary race candidates are fulfilled. When a race gets in that state, an update for the given
 * AnniversaryRaceDeterminator is triggered. Races that are either in a state that mets the prerequisites for
 * anniversary races or races that are finished are not observed anymore.
 */
public class RaceChangeObserverForAnniversaryDetection extends AbstractTrackedRegattaAndRaceObserver {

    /**
     * Listeners added for {@link TrackedRace}s that need to be cleaned when {@link TrackedRace}s are removed.
     */
    private final Map<TrackedRace, Listener> listeners;
    private final AnniversaryRaceDeterminatorImpl anniversaryRaceDeterminator;

    /**
     * Flag that indicates that the {@link RaceChangeObserverForAnniversaryDetection} is stopped which means that no
     * further updates should be triggered. This is e.g. the case when a server is converted to a replica.
     */
    private final AtomicBoolean stopped;

    public RaceChangeObserverForAnniversaryDetection(AnniversaryRaceDeterminatorImpl anniversaryRaceDeterminator) {
        this.anniversaryRaceDeterminator = anniversaryRaceDeterminator;
        this.listeners = new ConcurrentHashMap<>();
        this.stopped = new AtomicBoolean(false);
    }

    @Override
    protected void onRaceAdded(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRegatta trackedRegatta,
            DynamicTrackedRace trackedRace) {
        if (!stopped.get()) {
            // if the race initially complies with the prerequisites, we do not need to register a listener
            if (!handleRaceChange(trackedRace)) {
                Listener listener = new Listener(trackedRace);
                listeners.put(trackedRace, listener);
                trackedRace.addListener(listener);
            }
        }
    }

    @Override
    protected void onRaceRemoved(DynamicTrackedRace trackedRace) {
        removeListener(trackedRace);
        fireUpdateIfNotStopped();
    }

    /**
     * Removes {@link Listener} from given {@link TrackedRace}, if any.
     * 
     * @param trackedRace
     *            {@link TrackedRace} to remove {@link Listener} from
     */
    private void removeListener(TrackedRace trackedRace) {
        final Listener listener = listeners.remove(trackedRace);
        if (listener != null) {
            trackedRace.removeListener(listener);
        }
    }

    /**
     * {@link #fireUpdateIfNotStopped() Fires an update} if the race fulfills the criteria for being counted for
     * anniversary races.
     * 
     * @return {@code true} if the tracked race fulfills the criteria for being counted for anniversary races;
     *         {@code false} otherwise
     */
    private boolean handleRaceChange(TrackedRace trackedRace) {
        if (trackedRace.hasGPSData() && trackedRace.getStartOfRace() != null) {
            fireUpdateIfNotStopped();
            return true;
        }
        return false;
    }

    private void handleRaceChangeAndRemoveListenerIfNoLongerNeeded(TrackedRace trackedRace) {
        final TrackedRaceStatusEnum trackedRaceStatus = trackedRace.getStatus().getStatus();
        if (handleRaceChange(trackedRace) || trackedRaceStatus == TrackedRaceStatusEnum.FINISHED
                || trackedRaceStatus == TrackedRaceStatusEnum.REMOVED) {
            removeListener(trackedRace);
        }
    }

    /**
     * Updates {@link AnniversaryRaceDeterminatorImpl} if not {@link #stopped stopped}.
     */
    private void fireUpdateIfNotStopped() {
        if (!stopped.get()) {
            anniversaryRaceDeterminator.update();
        }
    }
    
    /**
     * Clears all known {@link TrackedRegatta} and {@link TrackedRace} instances and stops to trigger updates of the
     * given {@link AnniversaryRaceDeterminatorImpl}. This is e.g. the case when a server is converted to a replica.
     */
    public void stop() {
        stopped.set(true);
        removeAll();
    }
    
    /**
     * Clears all known {@link TrackedRegatta} and {@link TrackedRace} instances. Any new race will still be found and
     * thus trigger further updates.
     */
    public void clear() {
        removeAll();
    }

    private class Listener extends AbstractRaceChangeListener {
        private final TrackedRace trackedRace;

        public Listener(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            handleRaceChangeAndRemoveListenerIfNoLongerNeeded(trackedRace);
        }

        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            handleRaceChangeAndRemoveListenerIfNoLongerNeeded(trackedRace);
        }
        
        @Override
        public void firstGPSFixReceived() {
            handleRaceChangeAndRemoveListenerIfNoLongerNeeded(trackedRace);
        }
    }
}
