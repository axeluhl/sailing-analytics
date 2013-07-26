package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class SelectionContextImpl implements SelectionContext {

    private TrackedRegatta trackedRegatta;
    private TrackedRace trackedRace;
    private Competitor competitor;

    public SelectionContextImpl(TrackedRegatta trackedRegatta, TrackedRace trackedRace, Competitor competitor) {
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;
        this.competitor = competitor;
    }

    @Override
    public TrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
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
