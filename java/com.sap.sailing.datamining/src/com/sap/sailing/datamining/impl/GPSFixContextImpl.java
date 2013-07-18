package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl implements GPSFixContext {
    
    private TrackedRace trackedRace;
    private Competitor competitor;

    public GPSFixContextImpl(TrackedRace trackedRace, Competitor competitor) {
        this.trackedRace = trackedRace;
        this.competitor = competitor;
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