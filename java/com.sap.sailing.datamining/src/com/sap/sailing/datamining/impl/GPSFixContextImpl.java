package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl implements GPSFixContext {
    
    private Event event;
    private TrackedRace race;
    private Competitor competitor;

    public GPSFixContextImpl(Event event, TrackedRace race, Competitor competitor) {
        this.event = event;
        this.race = race;
        this.competitor = competitor;
    }

    public Event getEvent() {
        return event;
    }
    
    @Override
    public TrackedRace getRace() {
        return race;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
}