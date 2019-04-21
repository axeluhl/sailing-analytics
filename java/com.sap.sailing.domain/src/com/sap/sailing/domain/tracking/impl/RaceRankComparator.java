package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
public class RaceRankComparator extends AbstractRaceRankComparator<Distance> {
    private final Map<Competitor, Distance> negatedWindwardDistanceFromStartOfLeg;
    private final WindLegTypeAndLegBearingCache cache;
    
    /**
     * The position of the leg's {@link Leg#getFrom() start} at the time the first competitor
     * passed it. This way we can cope with the waypoint's mark(s) being moved after all
     * competitors rounded it the last time, and we get a constant reference point regardless
     * of which competitor we're computing the distance from this point for.<p>
     * 
     * Starts out as {@code null} and will be calculated until not {@code null} anymore.
     */
    private Position startOfLeg;
    
    public RaceRankComparator(TrackedRace trackedRace, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        super(trackedRace, timePoint, /* lessIsBetter */ true);
        this.cache = cache;
        this.negatedWindwardDistanceFromStartOfLeg = new HashMap<Competitor, Distance>();
    }

    @Override
    protected Distance getComparisonValueForSameLeg(Competitor competitor) {
        Distance result = negatedWindwardDistanceFromStartOfLeg.get(competitor);
        if (result == null) {
            final TrackedLegOfCompetitor trackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, getTimePoint());
            if (trackedLegOfCompetitor != null) {
                final Position startOfLegPosition = getStartOfLeg(trackedLegOfCompetitor.getTrackedLeg());
                if (startOfLegPosition != null) {
                    Position estimatedCompetitorPosition = getTrackedRace().getTrack(competitor).getEstimatedPosition(getTimePoint(), false);
                    if (estimatedCompetitorPosition != null) {
                        // use the negative windward distance from the leg start
                        result = trackedLegOfCompetitor.getTrackedLeg().getAbsoluteWindwardDistance(
                                startOfLegPosition, estimatedCompetitorPosition,
                                getTimePoint(), WindPositionMode.LEG_MIDDLE, cache).scale(-1);
                        negatedWindwardDistanceFromStartOfLeg.put(competitor, result);
                    }
                }
            }
        }
        return result;
    }
    
    private Position getStartOfLeg(TrackedLeg trackedLeg) {
        if (startOfLeg == null) {
            final Waypoint legStartWaypoint = trackedLeg.getLeg().getFrom();
            final Iterable<MarkPassing> markPassingsForLegStart = getTrackedRace().getMarkPassingsInOrder(legStartWaypoint);
            if (!Util.isEmpty(markPassingsForLegStart)) {
                final MarkPassing firstMarkPassingForLegStart = markPassingsForLegStart.iterator().next();
                final TimePoint timePointOfFirstMarkPassingForLegStart = firstMarkPassingForLegStart.getTimePoint();
                startOfLeg = getTrackedRace().getApproximatePosition(legStartWaypoint, timePointOfFirstMarkPassingForLegStart);
            }
        }
        return startOfLeg;
    }
}
