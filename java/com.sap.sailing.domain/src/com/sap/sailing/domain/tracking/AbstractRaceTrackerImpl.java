package com.sap.sailing.domain.tracking;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;


public abstract class AbstractRaceTrackerImpl implements RaceTracker {
    @Override
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        Set<RegattaAndRaceIdentifier> result = new HashSet<RegattaAndRaceIdentifier>();
        for (RaceDefinition race : getRaces()) {
            TrackedRace trackedRace = getTrackedRegatta().getTrackedRace(race);
            if (trackedRace != null) {
                result.add(trackedRace.getRaceIdentifier());
            }
        }
        return result;
    }

    private DynamicTrackedRegatta trackedRegatta;
    
    public AbstractRaceTrackerImpl() {
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    protected void setTrackedRegatta(DynamicTrackedRegatta trackedRegatta) {
        this.trackedRegatta = trackedRegatta;
    }
}
