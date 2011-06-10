package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TracTracTrackedLegImpl implements TrackedLeg {
    private final Leg leg;
    private final Map<Competitor, TrackedLegOfCompetitor> trackedLegsOfCompetitors;
    private final TrackedRace trackedRace;
    
    public TracTracTrackedLegImpl(TracTracTrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        this.leg = leg;
        this.trackedRace = trackedRace;
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

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException {
        // TODO Auto-generated method stub
        return false;
    }

}
