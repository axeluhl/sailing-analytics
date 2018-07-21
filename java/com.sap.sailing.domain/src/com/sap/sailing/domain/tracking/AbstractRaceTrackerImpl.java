package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;


public abstract class AbstractRaceTrackerImpl extends AbstractRaceTrackerBaseImpl {

    public AbstractRaceTrackerImpl(RaceTrackingConnectivityParameters connectivityParams) {
        super(connectivityParams);
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        final RegattaAndRaceIdentifier result;
        final RaceDefinition race = getRace();
        if (race != null) {
            TrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
            if (trackedRace != null) {
                result = trackedRace.getRaceIdentifier();
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public abstract DynamicTrackedRegatta getTrackedRegatta();
}
