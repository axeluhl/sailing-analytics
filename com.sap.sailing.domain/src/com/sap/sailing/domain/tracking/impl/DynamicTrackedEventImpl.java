package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;

public class DynamicTrackedEventImpl extends TrackedEventImpl implements DynamicTrackedEvent {

    public DynamicTrackedEventImpl(Event event, long millisecondsOverWhichToAverageSpeed) {
        super(event, millisecondsOverWhichToAverageSpeed);
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RaceDefinition race) {
        return (DynamicTrackedRace) super.getTrackedRace(race);
    }

    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        super.addTrackedRace((DynamicTrackedRace) trackedRace);
    }

}
