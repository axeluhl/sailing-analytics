package com.sap.sailing.server.anniversary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.impl.TrackedRaces;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.AbstractTrackedRegattaAndRaceObserver;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;

/**
 * {@link AbstractTrackedRegattaAndRaceObserver} that observes {@link TrackedRace TrackedRaces} to determine when the
 * prerequisites for anniversary race candidates are fulfilled. When a race gets in that state, an update for the given
 * AnniversaryRaceDeterminator is triggered. Races that are either is a state that mets the prerequisites for
 * anniversary races or races that are finished are not observed anymore.
 */
public class RaceChangeObserverForAnniversaryDetection extends AbstractTrackedRegattaAndRaceObserver {

    /**
     * Listeners added a {@link TrackedRaces} that need to be cleaned when {@link TrackedRace}s are removed.
     */
    private final Map<TrackedRace, Listener> listeners;
    private final AnniversaryRaceDeterminator anniversaryRaceDeterminator;

    public RaceChangeObserverForAnniversaryDetection(AnniversaryRaceDeterminator anniversaryRaceDeterminator) {
        this.anniversaryRaceDeterminator = anniversaryRaceDeterminator;
        listeners = new ConcurrentHashMap<>();
    }

    @Override
    protected void onRaceAdded(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRegatta trackedRegatta,
            DynamicTrackedRace trackedRace) {
        // if the race initially complies with the prerequisites, we do not need to register a listener
        if (!handleRaceChange(trackedRace)) {
            Listener listener = new Listener(trackedRace);
            listeners.put(trackedRace, listener);
            trackedRace.addListener(listener);
        }
    }

    @Override
    protected void onRaceRemoved(DynamicTrackedRace trackedRace) {
        removeListener(trackedRace);
        fireUpdate();
    }

    private void removeListener(TrackedRace trackedRace) {
        final Listener listener = listeners.remove(trackedRace);
        if (listener != null) {
            trackedRace.removeListener(listener);
        }
    }

    /**
     * {@link #fireUpdate() Fires an update} if the race fulfills the criteria for being counted for anniversary races.
     * 
     * @return {@code true} if the tracked race fulfills the criteria for being counted for anniversary races;
     *         {@code false} otherwise
     */
    private boolean handleRaceChange(TrackedRace trackedRace) {
        if (trackedRace.hasGPSData() && trackedRace.getStartOfRace() != null) {
            fireUpdate();
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

    private void fireUpdate() {
        anniversaryRaceDeterminator.update();
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
