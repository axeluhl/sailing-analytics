package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedLegOfCompetitorContextImpl extends HasTrackedLegContextImpl implements HasTrackedLegOfCompetitorContext {

    private final TrackedLegOfCompetitor trackedLegOfCompetitor;
    private final Competitor competitor;

    public HasTrackedLegOfCompetitorContextImpl(HasTrackedLegContext trackedLegContext, TrackedLegOfCompetitor trackedLegOfCompetitor) {
        this(trackedLegContext.getEvent(), trackedLegContext.getRegatta(), trackedLegContext.getFleet(),
                trackedLegContext.getTrackedRace(), trackedLegContext.getTrackedLeg(),
                trackedLegContext.getLegNumber(), trackedLegOfCompetitor);
    }

    public HasTrackedLegOfCompetitorContextImpl(Event event, Regatta regatta, Fleet fleet, TrackedRace trackedRace,
            TrackedLeg trackedLeg, int legNumber, TrackedLegOfCompetitor trackedLegOfCompetitor) {
        super(event, regatta, fleet, trackedRace, trackedLeg, legNumber);
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.competitor = trackedLegOfCompetitor.getCompetitor();
    }
    
    @Override
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor() {
        return trackedLegOfCompetitor;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

}
