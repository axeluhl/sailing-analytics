package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
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

    protected Competitor getLeader(TimePoint timePoint) {
        TreeSet<TrackedLegOfCompetitor> byRank = getCompetitorTracksOrderedByRank(timePoint);
        return byRank.first().getCompetitor();
    }

    protected TreeSet<TrackedLegOfCompetitor> getCompetitorTracksOrderedByRank(TimePoint timePoint) {
        TreeSet<TrackedLegOfCompetitor> treeSet = new TreeSet<TrackedLegOfCompetitor>(new WindwardToGoComparator(timePoint));
        for (TrackedLegOfCompetitor competitorLeg : getTrackedLegsOfCompetitors()) {
            treeSet.add(competitorLeg);
        }
        return treeSet;
    }
    
    /**
     * Compares competitor tracks based on the windward distance they still have to go and/or leg completion times at a
     * given point in time.
     */
    private class WindwardToGoComparator implements Comparator<TrackedLegOfCompetitor> {
        private TimePoint timePoint;

        public WindwardToGoComparator(TimePoint timePoint) {
            this.timePoint = timePoint;
        }
        
        @Override
        public int compare(TrackedLegOfCompetitor o1, TrackedLegOfCompetitor o2) {
            try {
                int result;
                if (o1.hasFinishedLeg(timePoint)) {
                    if (o2.hasFinishedLeg(timePoint)) {
                        result = getTrackedRace().getMarkPassing(o1.getCompetitor(), getLeg().getTo()).getTimePoint().compareTo(
                                getTrackedRace().getMarkPassing(o2.getCompetitor(), getLeg().getTo()).getTimePoint());
                    } else {
                        result = -1; // o1 < o2 because o1 already finished the leg but o2 didn't
                    }
                } else if (o2.hasFinishedLeg(timePoint)) {
                    result = 1; // o1 > o2 because o2 already finished the leg but o1 didn't
                } else {
                    // both didn't finish the leg yet:
                    Distance o1d = o1.getWindwardDistanceToGo(timePoint);
                    Distance o2d = o2.getWindwardDistanceToGo(timePoint);
                    result = o1d.compareTo(o2d);
                }
                return result;
            } catch (NoWindException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
