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
    private final DummyMarkPassingWithTimePointOnly markPassingWithTimePoint;
    
    public RaceRankComparator(TrackedRace trackedRace, TimePoint timePoint) {
        super();
        this.trackedRace = trackedRace;
        this.timePoint = timePoint;
        this.markPassingWithTimePoint = new DummyMarkPassingWithTimePointOnly(timePoint);
    }

    @Override
    public int compare(Competitor o1, Competitor o2) {
        NavigableSet<MarkPassing> o1MarkPassings = trackedRace.getMarkPassings(o1).headSet(markPassingWithTimePoint, /* inclusive */ true);
        NavigableSet<MarkPassing> o2MarkPassings = trackedRace.getMarkPassings(o2).headSet(markPassingWithTimePoint, /* inclusive */ true);
        int result = o2MarkPassings.size() - o1MarkPassings.size(); // inverted: more legs means smaller rank
        if (result == 0 && o1MarkPassings.size() > 0) {
            // Competitors are on same leg and both have already started the first leg.
            // TrackedLegOfCompetitor comparison also correctly uses finish times for a leg
            // in case we have the final leg, so both competitors finished the race.
            TrackedLegOfCompetitor o1Leg = trackedRace.getCurrentLeg(o1, timePoint);
            if (o1Leg == null) {
                // both must already finished race; sort by race finish time: earlier time means smaller (better) rank
                result = o1MarkPassings.last().getTimePoint().compareTo(o2MarkPassings.last().getTimePoint());
            } else {
                TrackedLegOfCompetitor o2Leg = trackedRace.getCurrentLeg(o2, timePoint);
                result = new WindwardToGoComparator(trackedRace.getTrackedLeg(o1Leg.getLeg()), timePoint).compare(
                        o1Leg, o2Leg);
            }
        }
        return result;
    }
}
