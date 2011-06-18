package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.SortedSet;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindError;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

/**
 * Compares competitor tracks based on the windward distance they still have to go and/or leg completion times at a
 * given point in time. Two tracks of different competitors will never be considered equal because otherwise
 * the one may replace the other in a {@link SortedSet}. Therefore, if by the criteria normally employed by this
 * comparator the two are equal, the comparator instead sorts by the names of the competitors.
 */
public class WindwardToGoComparator implements Comparator<TrackedLegOfCompetitor> {
    private final TrackedLeg trackedLeg;
    private final TimePoint timePoint;

    public WindwardToGoComparator(TrackedLeg trackedLeg, TimePoint timePoint) {
        this.trackedLeg = trackedLeg;
        this.timePoint = timePoint;
    }
    
    @Override
    public int compare(TrackedLegOfCompetitor o1, TrackedLegOfCompetitor o2) {
        try {
            int result;
            if (o1.hasFinishedLeg(timePoint)) {
                if (o2.hasFinishedLeg(timePoint)) {
                    result = trackedLeg.getTrackedRace().getMarkPassing(o1.getCompetitor(), trackedLeg.getLeg().getTo()).getTimePoint().compareTo(
                            trackedLeg.getTrackedRace().getMarkPassing(o2.getCompetitor(), trackedLeg.getLeg().getTo()).getTimePoint());
                } else {
                    result = -1; // o1 < o2 because o1 already finished the leg but o2 didn't
                }
            } else if (o2.hasFinishedLeg(timePoint)) {
                result = 1; // o1 > o2 because o2 already finished the leg but o1 didn't
            } else {
                // both didn't finish the leg yet:
                Distance o1d = o1.getWindwardDistanceToGo(timePoint);
                Distance o2d = o2.getWindwardDistanceToGo(timePoint);
                result = o1d.compareTo(o2d); // smaller distance to go means smaller rank
            }
            if (result == 0 && o1.getCompetitor() != o2.getCompetitor()) {
                result = o1.getCompetitor().getName().compareTo(o2.getCompetitor().getName());
            }
            return result;
        } catch (NoWindException e) {
            throw new NoWindError(e);
        }
    }
}
