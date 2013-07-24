package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class SelectionContextImpl implements SelectionContext {

    private TrackedRegatta trackedRegatta;
    private TrackedRace trackedRace;
    private Competitor competitor;
    private TrackedLeg trackedLeg;

    public SelectionContextImpl(TrackedRegatta trackedRegatta) {
        this.trackedRegatta = trackedRegatta;
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

    @Override
    public TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    @Override
    public void setTrackedRace(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }

    @Override
    public void setTrackedLeg(TrackedLeg trackedLeg) {
        this.trackedLeg = trackedLeg;
    }

}
