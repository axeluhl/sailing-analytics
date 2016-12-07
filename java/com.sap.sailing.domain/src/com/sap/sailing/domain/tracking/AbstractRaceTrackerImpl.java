package com.sap.sailing.domain.tracking;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;


public abstract class AbstractRaceTrackerImpl extends AbstractRaceTrackerBaseImpl {

    @Override
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        Set<RegattaAndRaceIdentifier> result = new HashSet<RegattaAndRaceIdentifier>();
        final Set<RaceDefinition> races = getRaces();
        if (races != null) {
            for (RaceDefinition race : races) {
                TrackedRace trackedRace = getTrackedRegatta().getTrackedRace(race);
                if (trackedRace != null) {
                    result.add(trackedRace.getRaceIdentifier());
                }
            }
        }
        return result;
    }

    @Override
    public abstract DynamicTrackedRegatta getTrackedRegatta();
}
