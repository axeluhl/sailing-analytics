package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.util.Util;

public abstract class AbstractRaceTrackerImpl implements RaceTracker {
    private DynamicTrackedEvent trackedEvent;
    
    /**
     * Used during {@link #stop} to remove the {@link #trackedEvent} if it has no more tracked races
     */
    private final TrackedEventRegistry trackedEventRegistry;
    
    public AbstractRaceTrackerImpl(TrackedEventRegistry trackedEventRegistry) {
        this.trackedEventRegistry = trackedEventRegistry;
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    protected void setTrackedEvent(DynamicTrackedEvent trackedEvent) {
        this.trackedEvent = trackedEvent;
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        Set<RaceDefinition> races = getRaces();
        if (races != null && !races.isEmpty()) {
            for (RaceDefinition race : races) {
                TrackedRace trackedRace = trackedEvent.getExistingTrackedRace(race);
                if (trackedRace != null) {
                    trackedEvent.removedTrackedRace(trackedRace);
                }
            }
            if (Util.isEmpty(trackedEvent.getTrackedRaces())) {
                trackedEventRegistry.remove(trackedEvent.getEvent());
            }
        }
    }

}
