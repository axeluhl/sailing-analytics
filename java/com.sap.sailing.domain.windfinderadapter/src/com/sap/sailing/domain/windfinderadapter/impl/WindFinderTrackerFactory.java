package com.sap.sailing.domain.windfinderadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;

public class WindFinderTrackerFactory implements WindTrackerFactory {
    private final Map<RaceDefinition, WindTracker> windTrackerPerRace;
    
    public WindFinderTrackerFactory() {
        windTrackerPerRace = new HashMap<>();
    }

    @Override
    public WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race,
            boolean correctByDeclination) throws Exception {
        final WindTracker result;
        synchronized (windTrackerPerRace) {
            final WindTracker existingWindTrackerForRace = getExistingWindTracker(race);
            if (existingWindTrackerForRace == null) {
                final DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(race);
                result = new WindFinderWindTracker(trackedRace, correctByDeclination, this);
                windTrackerPerRace.put(race, result);
            } else {
                result = existingWindTrackerForRace;
            }
        }
        return result;
    }

    @Override
    public WindTracker getExistingWindTracker(RaceDefinition race) {
        synchronized (windTrackerPerRace) {
            return windTrackerPerRace.get(race);
        }
    }
    
    void trackerStopped(RaceDefinition race) {
        synchronized (windTrackerPerRace) {
            windTrackerPerRace.remove(race);
        }
    }

}
