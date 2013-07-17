package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl implements GPSFixContext {
    
    private Event event;
    private TrackedRace trackedRace;
    private Competitor competitor;

    public GPSFixContextImpl(Event event, TrackedRace trackedRace, Competitor competitor) {
        this.event = event;
        this.trackedRace = trackedRace;
        this.competitor = competitor;
    }

    public Event getEvent() {
        return event;
    }
    
    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
}