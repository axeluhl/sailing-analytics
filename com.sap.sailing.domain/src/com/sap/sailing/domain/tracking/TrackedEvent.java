package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;

public interface TrackedEvent {
    Event getEvent();
    Iterable<TrackedRace> getTrackedRaces();
    Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass);
}
