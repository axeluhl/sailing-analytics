package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

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
    
    @Override
    public TrackedRace getTrackedRace() {
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

    protected Competitor getLeader(TimePoint timePoint) {
        TreeSet<TrackedLegOfCompetitor> byRank = getCompetitorTracksOrderedByRank(timePoint);
        return byRank.first().getCompetitor();
    }

    /**
     * Orders the tracked legs for all competitors for this tracked leg for the given time point. This
     * results in an order that gives a ranking for this tracked leg. In particular, boats that have not
     * yet entered this leg will all be ranked equal because their windward distance to go is the full
     * leg's winward distance. Boats who already finished this leg have their tracks ordered by the time
     * points at which they finished the leg.<p>
     * 
     * Note that this does not reflect overall race standings. For that, the ordering would have to
     * consider the order of the boats not currently in this leg, too.
     */
    protected TreeSet<TrackedLegOfCompetitor> getCompetitorTracksOrderedByRank(TimePoint timePoint) {
        TreeSet<TrackedLegOfCompetitor> treeSet = new TreeSet<TrackedLegOfCompetitor>(new WindwardToGoComparator(this, timePoint));
        for (TrackedLegOfCompetitor competitorLeg : getTrackedLegsOfCompetitors()) {
            treeSet.add(competitorLeg);
        }
        return treeSet;
    }
    
}
