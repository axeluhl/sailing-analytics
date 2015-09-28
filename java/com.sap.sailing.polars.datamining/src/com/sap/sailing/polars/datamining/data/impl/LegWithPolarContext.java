package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;

public class LegWithPolarContext implements HasLegPolarContext {
    
    private final Leg leg;
    private final TrackedRace trackedRace;
    private final HasFleetPolarContext fleetPolarContext;

    public LegWithPolarContext(Leg leg, TrackedRace trackedRace, HasFleetPolarContext fleetPolarContext) {
        this.leg = leg;
        this.trackedRace = trackedRace;
        this.fleetPolarContext = fleetPolarContext;
    }

    @Override
    public Leg getLeg() {
        return leg;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Integer getLegIndex() {
        return leg.getZeroBasedIndexOfStartWaypoint();
    }

    @Override
    public HasFleetPolarContext getFleetPolarContext() {
        return fleetPolarContext;
    }

}
