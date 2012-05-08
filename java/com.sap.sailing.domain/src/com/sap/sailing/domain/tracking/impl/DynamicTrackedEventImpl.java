package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;

public class DynamicTrackedEventImpl extends TrackedEventImpl implements DynamicTrackedEvent {
    private static final long serialVersionUID = -90155868534737120L;

    public DynamicTrackedEventImpl(Event event) {
        super(event);
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getTrackedRace(race);
    }

    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        super.addTrackedRace((DynamicTrackedRace) trackedRace);
    }

    @Override
    public DynamicTrackedRace getExistingTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getExistingTrackedRace(race);
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        DynamicTrackedRace result = (DynamicTrackedRace) super.createTrackedRace(raceDefinition, windStore, millisecondsOverWhichToAverageWind,
                millisecondsOverWhichToAverageSpeed, raceDefinitionSetToUpdate);
        addTrackedRace(result);
        return result;
    }
}
