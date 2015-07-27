package com.sap.sailing.datamining.impl.data;

import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Speed;

public class RaceOfCompetitorWithContext implements HasRaceOfCompetitorContext {

    private final HasTrackedRaceContext trackedRaceContext;
    private final Competitor competitor;

    public RaceOfCompetitorWithContext(HasTrackedRaceContext trackedRaceContext, Competitor competitor) {
        this.trackedRaceContext = trackedRaceContext;
        this.competitor = competitor;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public double getDistanceToStartLineAtStart() {
        return getTrackedRaceContext().getTrackedRace().getDistanceToStartLine(getCompetitor(), 0).getMeters();
    }
    
    @Override
    public Speed getSpeedAtStart() {
        return getTrackedRaceContext().getTrackedRace().getSpeed(getCompetitor(), 0);
    }
    
    @Override
    public Speed getSpeedTenSecondsBeforeStart() {
        return getTrackedRaceContext().getTrackedRace().getSpeed(getCompetitor(), TimeUnit.SECONDS.toMillis(10));
    }

}