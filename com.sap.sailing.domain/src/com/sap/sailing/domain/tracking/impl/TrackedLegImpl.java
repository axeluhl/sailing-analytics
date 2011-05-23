package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TrackedLegImpl implements TrackedLeg {
    private final Leg leg;
    private final Map<Competitor, TrackedLegOfCompetitor> trackedLegsOfCompetitors;
    private TrackedRaceImpl trackedRace;
    
    public TrackedLegImpl(TrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        super();
        this.leg = leg;
        this.trackedRace = trackedRace;
        trackedLegsOfCompetitors = new HashMap<Competitor, TrackedLegOfCompetitor>();
        for (Competitor competitor : competitors) {
            trackedLegsOfCompetitors.put(competitor, new TrackedLegOfCompetitorImpl(this, competitor));
        }
    }
    
    @Override
    public Leg getLeg() {
        return leg;
    }
    
    protected TrackedRaceImpl getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors() {
        return trackedLegsOfCompetitors.values();
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor) {
        return trackedLegsOfCompetitors.get(competitor);
    }

    @Override
    public int getRankAtBeginningOfLeg(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRankAtEndOfLeg(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

}
