package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.TimePoint;

/**
 * Compares two competitors by their ranking in the overall race for a given time point. Competitors who haven't started
 * the first leg are all equally ranked last. Competitors in different legs are ranked by inverse leg index: the higher
 * the number of the leg the lesser (better) the rank. Competitors in the same leg are ranked by their windward distance
 * to go in that leg, requiring wind data or estimates to be available for the given time point. The wind is estimated at
 * the middle of the leg for consistent ordering, so the same wind data will be used for comparing all competitors in the
 * same leg at a given point in time.
 * <p>
 * 
 * Two different competitors may end up being ranked equal by this comparator. So take care and don't use this
 * comparator class when inserting into an {@link SortedSet} when you want something like a ranking. It may overwrite
 * existing entries. Use {@link Collections#sort(java.util.List, Comparator)} instead.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RaceRankComparator implements Comparator<Competitor> {
    private final TrackedRace trackedRace;
    private final TimePoint timePoint;
    private final DummyMarkPassingWithTimePointOnly markPassingWithTimePoint;
    private final Map<Competitor, Distance> windwardDistanceToGoInLegCache;
    
    public RaceRankComparator(TrackedRace trackedRace, TimePoint timePoint) throws NoWindException {
        super();
        this.trackedRace = trackedRace;
        this.timePoint = timePoint;
        this.markPassingWithTimePoint = new DummyMarkPassingWithTimePointOnly(timePoint);
        this.windwardDistanceToGoInLegCache = new HashMap<Competitor, Distance>();
        LeaderboardDTOCalculationReuseCache cache = new LeaderboardDTOCalculationReuseCache(timePoint);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, timePoint);
            if (trackedLegOfCompetitor != null) {
                windwardDistanceToGoInLegCache.put(competitor, trackedLegOfCompetitor.getWindwardDistanceToGo(timePoint,
                        WindPositionMode.LEG_MIDDLE, cache));
            }
        }
    }

    @Override
    public int compare(Competitor o1, Competitor o2) {
        int result;
        if (o1 == o2) {
            result = 0;
        } else {
            NavigableSet<MarkPassing> o1MarkPassings = trackedRace.getMarkPassings(o1);
            NavigableSet<MarkPassing> o1MarkPassingsBeforeTimePoint;
            MarkPassing o1LastMarkPassingBeforeTimePoint = null;
            int o1MarkPassingsBeforeTimePointSize;
            TrackedLegOfCompetitor o1Leg;
            trackedRace.lockForRead(o1MarkPassings);
            try {
                o1MarkPassingsBeforeTimePoint = o1MarkPassings.headSet(
                        markPassingWithTimePoint, /* inclusive */true);
                o1MarkPassingsBeforeTimePointSize = o1MarkPassingsBeforeTimePoint.size();
                if (o1MarkPassingsBeforeTimePointSize > 0) {
                    o1LastMarkPassingBeforeTimePoint = o1MarkPassingsBeforeTimePoint.last();
                }
                o1Leg = trackedRace.getCurrentLeg(o1, timePoint);
            } finally {
                trackedRace.unlockAfterRead(o1MarkPassings);
            }
            NavigableSet<MarkPassing> o2MarkPassings = trackedRace.getMarkPassings(o2);
            NavigableSet<MarkPassing> o2MarkPassingsBeforeTimePoint;
            MarkPassing o2LastMarkPassingBeforeTimePoint = null;
            int o2MarkPassingsBeforeTimePointSize;
            TrackedLegOfCompetitor o2Leg;
            trackedRace.lockForRead(o2MarkPassings);
            try {
                o2MarkPassingsBeforeTimePoint = o2MarkPassings.headSet(markPassingWithTimePoint, /* inclusive */true);
                o2MarkPassingsBeforeTimePointSize = o2MarkPassingsBeforeTimePoint.size();
                if (o2MarkPassingsBeforeTimePointSize > 0) {
                    o2LastMarkPassingBeforeTimePoint = o2MarkPassingsBeforeTimePoint.last();
                }
                o2Leg = trackedRace.getCurrentLeg(o2, timePoint);
            } finally {
                trackedRace.unlockAfterRead(o2MarkPassings);
            }
            result = o2MarkPassingsBeforeTimePointSize - o1MarkPassingsBeforeTimePointSize; // inverted: more legs means
                                                                                            // smaller rank
            if (result == 0 && o1MarkPassingsBeforeTimePointSize > 0) {
                // Competitors are on same leg and both have already started the first leg.
                // TrackedLegOfCompetitor comparison also correctly uses finish times for a leg
                // in case we have the final leg, so both competitors finished the race.
                if (o1Leg == null) {
                    // both must already have finished race; sort by race finish time: earlier time means smaller
                    // (better) rank
                    result = o1LastMarkPassingBeforeTimePoint.getTimePoint().compareTo(
                            o2LastMarkPassingBeforeTimePoint.getTimePoint());
                } else {
                    if (o2Leg == null) {
                        result = 1; // o1Leg != null, so o1 has started leg already, o2 hasn't
                    } else {
                        if (o1Leg.getLeg() != o2Leg.getLeg()) {
                            // strange: both have the same number of mark passings but are in different legs; something is
                            // broken, but we can only try our best:
                            result = trackedRace.getRace().getCourse().getLegs().indexOf(o1Leg.getLeg()) -
                                    trackedRace.getRace().getCourse().getLegs().indexOf(o2Leg.getLeg());
                        } else {
                            // competitors are in same leg; compare their windward distance to go
                            final Distance wwdtgO1 = windwardDistanceToGoInLegCache.get(o1);
                            final Distance wwdtgO2 = windwardDistanceToGoInLegCache.get(o2);
                            result = wwdtgO1==null?wwdtgO2==null?0:-1:wwdtgO2==null?1:wwdtgO1.compareTo(wwdtgO2);
                        }
                    }
                }
            }
        }
        return result;
    }
}
