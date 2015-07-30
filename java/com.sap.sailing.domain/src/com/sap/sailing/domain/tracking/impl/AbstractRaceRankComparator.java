package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
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
 * This comparator assumes that the race's {@link Course} remains unchanged from the time this comparator's constructor
 * is invoked and the last call to {@link #compare(Competitor, Competitor)} on this comparator. This should be obvious
 * because course changes have an effect on the race's leg structure and therefore on the foundations for ranking.
 * Clients should use {@link Course#lockForRead()} and {@link Course#unlockAfterRead()} to frame the construction and
 * use of this comparator.<p>
 * 
 * Two different competitors may end up being ranked equal by this comparator. So take care and don't use this
 * comparator class when inserting into an {@link SortedSet} when you want something like a ranking. It may overwrite
 * existing entries. Use {@link Collections#sort(java.util.List, Comparator)} instead.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractRaceRankComparator<C extends Comparable<C>> implements Comparator<Competitor> {
    private static final Logger logger = Logger.getLogger(AbstractRaceRankComparator.class.getName());
    private final TrackedRace trackedRace;
    private final TimePoint timePoint;
    private final DummyMarkPassingWithTimePointOnly markPassingWithTimePoint;
    private final boolean lessIsBetter;
    
    /**
     * @param lessIsBetter
     *            tells whether lesser return values for {@link #getComparisonValueForSameLeg(Competitor)} denote a
     *            better competitor ranking or not.
     */
    public AbstractRaceRankComparator(TrackedRace trackedRace, TimePoint timePoint, boolean lessIsBetter) {
        super();
        this.trackedRace = trackedRace;
        this.timePoint = timePoint;
        this.markPassingWithTimePoint = new DummyMarkPassingWithTimePointOnly(timePoint);
        this.lessIsBetter = lessIsBetter;
    }

    @Override
    public int compare(Competitor o1, Competitor o2) {
        int result;
        if (o1 == o2) {
            result = 0;
        } else {
            final Course course = trackedRace.getRace().getCourse();
            NavigableSet<MarkPassing> o1MarkPassings = trackedRace.getMarkPassings(o1);
            NavigableSet<MarkPassing> o1MarkPassingsBeforeTimePoint;
            MarkPassing o1LastMarkPassingBeforeTimePoint = null;
            int o1MarkPassingsBeforeTimePointSize;
            TrackedLegOfCompetitor o1Leg;
            trackedRace.lockForRead(o1MarkPassings);
            try {
                // FIXME for the XMLExportTest.testExportingESSNice2014 the "inclusive" parameter doesn't seem to work properly; problem is caused by the MarkPassingByTimeComparator falling back to competitor ID if times are equal
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
            final List<Leg> legs = course.getLegs();
            result = o1LastMarkPassingBeforeTimePoint == null
                  ? o2LastMarkPassingBeforeTimePoint == null ? 0  // both haven't started yet
                                                             : 1  // only o2 has started; o1 is worse ("greater") than o2
                  : o2LastMarkPassingBeforeTimePoint == null ? -1 // only o1 has started; o1 is better ("less") than o2
                  /* both have started; any difference */    : course.getIndexOfWaypoint(o2LastMarkPassingBeforeTimePoint.getWaypoint()) -
                  /* in last waypoint passed?          */      course.getIndexOfWaypoint(o1LastMarkPassingBeforeTimePoint.getWaypoint());
            if (result == 0 && o1MarkPassingsBeforeTimePointSize > 0) {
                assert o2LastMarkPassingBeforeTimePoint != null;
                // Competitors are on same leg and both have already started the first leg.
                // TrackedLegOfCompetitor comparison also correctly uses finish times for a leg
                // in case we have the final leg, so both competitors finished the race.
                if (o1Leg == null) {
                    // both must already have finished race; sort by race finish time: earlier time means smaller
                    // (better) rank
                    assert o2Leg == null;
                    result = o1LastMarkPassingBeforeTimePoint.getTimePoint().compareTo(
                            o2LastMarkPassingBeforeTimePoint.getTimePoint());
                } else {
                    assert o2Leg != null; // otherwise, o1Leg!=null && o2Leg==null, but then o1 would have a finish mark passing and o2 not
                    if (o1Leg.getLeg() != o2Leg.getLeg()) {
                        logger.finest("Warning: competitors "+o1+" and "+o2+" in different legs ("+
                              o1Leg.getLeg()+" and "+o2Leg.getLeg()+", respectively) although based on their last mark passings before "+
                                timePoint+" ("+o1LastMarkPassingBeforeTimePoint+" and "+o2LastMarkPassingBeforeTimePoint+", respectively) "+
                                "they should be in the same leg");
                        // strange: both have the same number of mark passings but are in different legs; something is
                        // broken, but we can only try our best:
                        logger.finest("Warning: competitors "+o1+" and "+o2+" in different legs ("+
                                o1Leg.getLeg()+" and "+o2Leg.getLeg()+", respectively) although based on their last mark passings before "+
                                  timePoint+" ("+o1LastMarkPassingBeforeTimePoint+" and "+o2LastMarkPassingBeforeTimePoint+", respectively) "+
                                  "they should be in the same leg");
                        result = legs.indexOf(o1Leg.getLeg()) - legs.indexOf(o2Leg.getLeg());
                    } else {
                        // competitors are in same leg; compare their windward distance to go
                        final C wwdtgO1 = getComparisonValueForSameLeg(o1);
                        final C wwdtgO2 = getComparisonValueForSameLeg(o2);
                        result = wwdtgO1==null?wwdtgO2==null?0:-1:wwdtgO2==null?1:(lessIsBetter?1:-1)*wwdtgO1.compareTo(wwdtgO2);
                    }
                }
            }
        }
        return result;
    }

    protected abstract C getComparisonValueForSameLeg(Competitor competitor);
}
