package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Distance;
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
public class RaceRankComparator extends AbstractRaceRankComparator<Distance> {
    private final Map<Competitor, Distance> windwardDistanceToGoInLegCache;
    private final WindLegTypeAndLegBearingCache cache;
    
    public RaceRankComparator(TrackedRace trackedRace, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        super(trackedRace, timePoint, /* lessIsBetter */ true);
        this.cache = cache;
        this.windwardDistanceToGoInLegCache = new HashMap<Competitor, Distance>();
    }

    @Override
    protected Distance getComparisonValueForSameLeg(Competitor competitor) {
        Distance result = windwardDistanceToGoInLegCache.get(competitor);
        if (result == null) {
            final TrackedLegOfCompetitor trackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, getTimePoint());
            if (trackedLegOfCompetitor != null) {
                result = trackedLegOfCompetitor.getWindwardDistanceToGo(getTimePoint(), WindPositionMode.LEG_MIDDLE, cache);
                windwardDistanceToGoInLegCache.put(competitor, result);
            }
        }
        return result;
    }
}
