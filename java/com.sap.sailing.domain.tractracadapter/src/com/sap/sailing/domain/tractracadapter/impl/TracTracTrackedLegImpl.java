package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TracTracTrackedLegImpl implements TrackedLeg {
    private static final long serialVersionUID = 2396033396354531099L;
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

    @Override
    public LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint) {
        LinkedHashMap<Competitor, Integer> result = new LinkedHashMap<Competitor, Integer>();
        for (Competitor c : getTrackedRace().getRace().getCompetitors()) {
            result.put(c, getTrackedLeg(c).getRank(timePoint));
        }
        return result;
    }

    @Override
    public LegType getLegType(TimePoint at) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bearing getLegBearing(TimePoint at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getCrossTrackError(Position p, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

}
