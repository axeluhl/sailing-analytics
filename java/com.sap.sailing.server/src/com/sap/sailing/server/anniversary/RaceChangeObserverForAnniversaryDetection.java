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

public class RaceChangeObserverForAnniversaryDetection extends AbstractTrackedRegattaAndRaceObserver {

    /**
     * Listeners added a {@link TrackedRaces} that need to be cleaned when {@link TrackedRace}s are removed.
     */
    private final Map<TrackedRace, Listener> listeners;

    public RaceChangeObserverForAnniversaryDetection() {
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

    private boolean handleRaceChange(TrackedRace trackedRace) {
        if (trackedRace.hasGPSData() && trackedRace.getStartOfRace() != null) {
            fireUpdate();
            return true;
        }
        return false;
    }

    private void handleRaceChangeAndRemoveListener(TrackedRace trackedRace) {
        final TrackedRaceStatusEnum trackedRaceStatus = trackedRace.getStatus().getStatus();
        if (handleRaceChange(trackedRace) || trackedRaceStatus == TrackedRaceStatusEnum.FINISHED
                || trackedRaceStatus == TrackedRaceStatusEnum.REMOVED) {
            removeListener(trackedRace);
        }
    }

    private void fireUpdate() {
        // FIXME implement
    }

    private class Listener extends AbstractRaceChangeListener {
        private final TrackedRace trackedRace;

        public Listener(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            handleRaceChangeAndRemoveListener(trackedRace);
        }

        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            handleRaceChangeAndRemoveListener(trackedRace);
        }
        
        @Override
        public void firstGPSFixReceived() {
            handleRaceChangeAndRemoveListener(trackedRace);
        }
    }
}
