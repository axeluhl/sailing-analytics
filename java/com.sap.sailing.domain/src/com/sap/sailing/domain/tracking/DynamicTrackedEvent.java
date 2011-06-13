package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

public interface DynamicTrackedEvent extends TrackedEvent {

    DynamicTrackedRace getTrackedRace(RaceDefinition race);

}
