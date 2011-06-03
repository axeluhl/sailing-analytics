package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TracTracTrackedLegImpl implements TrackedLeg {
    private final Leg leg;
    private final Map<Competitor, TrackedLegOfCompetitor> trackedLegsOfCompetitors;
    
    public TracTracTrackedLegImpl(TracTracTrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        this.leg = leg;
        trackedLegsOfCompetitors = new HashMap<Competitor, TrackedLegOfCompetitor>();
        for (Competitor competitor : competitors) {
            trackedLegsOfCompetitors.put(competitor, new TracTracTrackedLegOfCompetitor(trackedRace, leg, competitor));
        }
    }

    @Override
    public Leg getLeg() {
        return leg;
    }

    @Override
    public Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors() {
        return trackedLegsOfCompetitors.values();
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor) {
        return trackedLegsOfCompetitors.get(competitor);
    }

}
