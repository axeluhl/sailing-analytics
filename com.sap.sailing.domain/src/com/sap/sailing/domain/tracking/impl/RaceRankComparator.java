package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Compares two competitors by their ranking in the overall race for a given time point. Competitors who haven't started
 * the first leg are all equally ranked last. Competitors in different legs are ranked by inverse leg index: the higher
 * the number of the leg the lesser (better) the rank. Competitors in the same leg are ranked by their windward distance
 * to go in that leg, requiring wind data or estimates to be available for the given time point.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceRankComparator implements Comparator<Competitor> {
    private final TrackedRace trackedRace;
    private final TimePoint timePoint;
    
    public RaceRankComparator(TrackedRace trackedRace, TimePoint timePoint) {
        super();
        this.trackedRace = trackedRace;
        this.timePoint = timePoint;
    }

    @Override
    public int compare(Competitor o1, Competitor o2) {
        NavigableSet<MarkPassing> o1MarkPassings = trackedRace.getMarkPassings(o1);
        NavigableSet<MarkPassing> o2MarkPassings = trackedRace.getMarkPassings(o2);
        int result = o1MarkPassings.size() - o2MarkPassings.size();
        if (result == 0 && o1MarkPassings.size() > 0) {
            // Competitors are on same leg and both have already started the first leg.
            // TrackedLegOfCompetitor comparison also correctly uses finish times for a leg
            // in case we have the final leg, so both competitors finished the race.
            TrackedLegOfCompetitor o1Leg = trackedRace.getCurrentLeg(o1, timePoint);
            TrackedLegOfCompetitor o2Leg = trackedRace.getCurrentLeg(o2, timePoint);
            result = new WindwardToGoComparator(trackedRace.getTrackedLeg(o1Leg.getLeg()), timePoint).compare(o1Leg, o2Leg);
        }
        return result;
    }
}
