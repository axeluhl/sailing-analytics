package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;

public abstract class AbstractRaceTrackerImpl implements RaceTracker {
    private DynamicTrackedEvent trackedEvent;
    
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
        }
    }

}
