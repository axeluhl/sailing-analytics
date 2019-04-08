package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * Compares competitor tracks based on the windward distance they still have to go and/or leg completion times at a
 * given point in time for the same leg. Two tracks of different competitors may end up being ranked equal by this
 * comparator. So take care and don't use this comparator class when inserting into an {@link SortedSet} when you want
 * something like a ranking. It may overwrite existing entries. Use {@link Collections#sort(java.util.List, Comparator)}
 * instead.
 */
public class WindwardToGoComparator implements Comparator<TrackedLegOfCompetitor> {
    private final TrackedLeg trackedLeg;
    private final TimePoint timePoint;
    private final Map<TrackedLegOfCompetitor, Distance> wwdtgCache;
    private final WindLegTypeAndLegBearingCache windAndLegTypeCache;

    public WindwardToGoComparator(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        this.trackedLeg = trackedLeg;
        this.timePoint = timePoint;
        wwdtgCache = new HashMap<TrackedLegOfCompetitor, Distance>();
        windAndLegTypeCache = cache;
    }
    
    @Override
    public int compare(TrackedLegOfCompetitor o1, TrackedLegOfCompetitor o2) {
        assert o1.getLeg() == o2.getLeg();
        try {
            int result;
            // To avoid having to synchronize on the competitors' mark passings, grab the decisive mark passings up front and
            // derive hasStarted / hasFinished from them; this is redundant to TrackedLegOfCompetitor.hasStarted/hasFinished but
            // avoids the heavy-weight synchronization-based "locking". It shall help avoid NPEs in case the mark passings
            // for a competitor change after deciding hasFinished and grabbing the mark passing (see bug 875).
            final MarkPassing o1MarkPassingForLegEnd = trackedLeg.getTrackedRace().getMarkPassing(o1.getCompetitor(), trackedLeg.getLeg().getTo());
            final MarkPassing o2MarkPassingForLegEnd = trackedLeg.getTrackedRace().getMarkPassing(o2.getCompetitor(), trackedLeg.getLeg().getTo());
            final boolean o1HasFinishedLeg = o1MarkPassingForLegEnd != null && o1MarkPassingForLegEnd.getTimePoint().compareTo(timePoint) <= 0;
            final boolean o2HasFinishedLeg = o2MarkPassingForLegEnd != null && o2MarkPassingForLegEnd.getTimePoint().compareTo(timePoint) <= 0;
            if (o1HasFinishedLeg) {
                if (o2HasFinishedLeg) {
                    result = o1MarkPassingForLegEnd.getTimePoint().compareTo(o2MarkPassingForLegEnd.getTimePoint());
                } else {
                    result = -1; // o1 < o2 because o1 already finished the leg but o2 didn't
                }
            } else if (o2HasFinishedLeg) {
                result = 1; // o1 > o2 because o2 already finished the leg but o1 didn't
            } else {
                // both didn't finish the leg yet; check which one has started:
                if (o1.hasStartedLeg(timePoint)) {
                    if (o2.hasStartedLeg(timePoint)) {
                        Distance o1d = getWindwardDistanceToGo(o1);
                        Distance o2d = getWindwardDistanceToGo(o2);
                        result = o1d==null?(o2d==null?0:1):o2d==null?1:o1d.compareTo(o2d); // smaller distance to go means smaller rank
                    } else {
                        result = -1;
                    }
                } else if (o2.hasStartedLeg(timePoint)) {
                    result = 1;
                } else {
                    // both did not start the leg:
                    result = 0;
                }
            }
            return result;
        } catch (NoWindException e) {
            throw new NoWindError(e);
        }
    }

    private Distance getWindwardDistanceToGo(TrackedLegOfCompetitor o1) throws NoWindException {
        Distance result = wwdtgCache.get(o1);
        if (result == null) {
            result = o1.getWindwardDistanceToGo(timePoint, WindPositionMode.LEG_MIDDLE, windAndLegTypeCache);
            wwdtgCache.put(o1, result);
        }
        return result;
    }
}
