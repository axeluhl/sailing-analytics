package com.sap.sailing.domain.tracking;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;


public abstract class AbstractRaceTrackerImpl implements RaceTracker {
    @Override
    public Set<EventAndRaceIdentifier> getRaceIdentifiers() {
        Set<EventAndRaceIdentifier> result = new HashSet<EventAndRaceIdentifier>();
        for (RaceDefinition race : getRaces()) {
            TrackedRace trackedRace = getTrackedEvent().getTrackedRace(race);
            if (trackedRace != null) {
                result.add(trackedRace.getRaceIdentifier());
            }
        }
        return result;
    }

    private DynamicTrackedEvent trackedEvent;
    
    public AbstractRaceTrackerImpl() {
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    protected void setTrackedEvent(DynamicTrackedEvent trackedEvent) {
        this.trackedEvent = trackedEvent;
    }
}
