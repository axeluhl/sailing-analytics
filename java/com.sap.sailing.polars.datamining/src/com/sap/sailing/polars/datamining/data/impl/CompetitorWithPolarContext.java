package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;

public class CompetitorWithPolarContext implements HasCompetitorPolarContext {
    
    private final Competitor competitor;
    private final TrackedRace trackedRace;
    private final Leg leg;
    private final HasLegPolarContext legPolarContext;

    public CompetitorWithPolarContext(Competitor competitor, TrackedRace trackedRace, Leg leg,
            HasLegPolarContext legPolarContext) {
        this.competitor = competitor;
        this.trackedRace = trackedRace;
        this.leg = leg;
        this.legPolarContext = legPolarContext;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Leg getLeg() {
        return leg;
    }

    @Override
    public HasLegPolarContext getLegPolarContext() {
        return legPolarContext;
    }

}
