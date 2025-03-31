package com.sap.sailing.server.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.tracking.TrackedRace;

public class WaitForTrackedRaceUtil {

    private WaitForTrackedRaceUtil() {
    }

    /**
     * Waits for a {@link TrackedRace} to show up for the given {@link RaceColumn} and {@link Fleet}. If no
     * {@link TrackedRace} shows up within the given amount of seconds, null is returned.
     */
    @SuppressWarnings("unchecked")
    public static <T extends TrackedRace> T waitForTrackedRace(RaceColumn raceColumn, Fleet fleet, int secondsToWait) {
        final CompletableFuture<TrackedRace> future = new CompletableFuture<>();

        final RaceColumnListener raceColumnListener = new WaitForTrackedRaceListener(fleet, future);
        raceColumn.addRaceColumnListener(raceColumnListener);
        final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
        if (trackedRace != null) {
            raceColumn.removeRaceColumnListener(raceColumnListener);
            return (T) trackedRace;
        }

        try {
            return (T) future.get(secondsToWait, TimeUnit.SECONDS);
        } catch (Exception e) {
            raceColumn.removeRaceColumnListener(raceColumnListener);
            return null;
        }
    }

    private static class WaitForTrackedRaceListener implements RaceColumnListenerWithDefaultAction {
        private static final long serialVersionUID = 1L;

        private final Fleet fleet;
        private final CompletableFuture<TrackedRace> future;

        public WaitForTrackedRaceListener(Fleet fleet, CompletableFuture<TrackedRace> future) {
            this.fleet = fleet;
            this.future = future;
        }

        @Override
        public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleetOfLinkedRace, TrackedRace trackedRace) {
            if (fleet == fleetOfLinkedRace) {
                raceColumn.removeRaceColumnListener(this);
                future.complete(trackedRace);
            }
        }

        @Override
        public void defaultAction() {
        }
        
        @Override
        public boolean isTransient() {
            return true;
        }
    }
}
