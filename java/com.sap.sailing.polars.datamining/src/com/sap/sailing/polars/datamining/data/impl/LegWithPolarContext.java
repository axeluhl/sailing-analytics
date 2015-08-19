package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;

public class LegWithPolarContext implements HasLegPolarContext {
    
    private final Leg leg;
    private final TrackedRace trackedRace;

    public LegWithPolarContext(Leg leg, TrackedRace trackedRace) {
        this.leg = leg;
        this.trackedRace = trackedRace;
    }

    @Override
    public Leg getLeg() {
        return leg;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

}
