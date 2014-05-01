package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasGPSFixContextImpl extends HasTrackedLegOfCompetitorContextImpl implements HasGPSFixContext {
    
    private final GPSFixMoving gpsFix;

    public HasGPSFixContextImpl(HasTrackedLegOfCompetitorContext legContext, GPSFixMoving gpsFix) {
        this(legContext.getEvent(), legContext.getRegatta(), legContext.getFleet(), legContext.getTrackedRace(),
                legContext.getTrackedLeg(), legContext.getLegNumber(), legContext.getTrackedLegOfCompetitor(), gpsFix);
    }

    public HasGPSFixContextImpl(Event event, Regatta regatta, Fleet fleet, TrackedRace trackedRace,
            TrackedLeg trackedLeg, int legNumber, TrackedLegOfCompetitor trackedLegOfCompetitor, GPSFixMoving gpsFix) {
        super(event, regatta, fleet, trackedRace, trackedLeg, legNumber, trackedLegOfCompetitor);
        this.gpsFix = gpsFix;
    }

    @Override
    public GPSFixMoving getGPSFix() {
        return gpsFix;
    }
    
}