package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl implements GPSFixContext {
    
    private TrackedRace trackedRace;
    private int legNumber;
    private LegType legType;
    private Competitor competitor;

    public GPSFixContextImpl(TrackedRace trackedRace, int legNumber, LegType legType, Competitor competitor) {
        this.trackedRace = trackedRace;
        this.legNumber = legNumber;
        this.legType = legType;
        this.competitor = competitor;
    }
    
    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public LegType getLegType() {
        return legType;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
}