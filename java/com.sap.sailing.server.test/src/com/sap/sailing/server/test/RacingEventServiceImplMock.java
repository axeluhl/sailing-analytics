package com.sap.sailing.server.test;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class RacingEventServiceImplMock extends RacingEventServiceImpl {

    public RacingEventServiceImplMock() {
        super();
    }

    public Map<String, Regatta> getEventsByNameMap() {
        return regattasByName;
    }

    public Map<Regatta, Set<RaceTracker>> getRaceTrackersByEventMap() {
        return raceTrackersByRegatta;
    }

    public Map<Object, RaceTracker> getRaceTrackersByIDMap() {
        return raceTrackersByID;
    }

    public Map<String, Regatta> getEventsByName() {
        return regattasByName;
    }
}
