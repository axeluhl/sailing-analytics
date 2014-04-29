package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasGPSFixContextImpl extends HasTrackedLegOfCompetitorContextImpl implements HasGPSFixContext {
    
    public HasGPSFixContextImpl(HasTrackedLegOfCompetitorContext legContext) {
        this(legContext.getEvent(), legContext.getRegatta(), legContext.getFleet(), legContext.getTrackedRace(),
                legContext.getTrackedLeg(), legContext.getLegNumber(), legContext.getCompetitor());
    }

    public HasGPSFixContextImpl(Event event, Regatta regatta, Fleet fleet, TrackedRace trackedRace,
            TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        super(event, regatta, fleet, trackedRace, trackedLeg, legNumber, competitor);
    }
    
}