package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindStore;

public class DynamicTrackedRegattaImpl extends TrackedRegattaImpl implements DynamicTrackedRegatta {
    private static final long serialVersionUID = -90155868534737120L;

    public DynamicTrackedRegattaImpl(Regatta regatta) {
        super(regatta);
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getTrackedRace(race);
    }

    @Override
    public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getExistingTrackedRace(race);
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        return (DynamicTrackedRace) super.createTrackedRace(raceDefinition, windStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate);
    }
}
