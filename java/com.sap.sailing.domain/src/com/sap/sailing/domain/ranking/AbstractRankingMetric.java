package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public abstract class AbstractRankingMetric implements RankingMetric {
    private static final long serialVersionUID = -3671039530564696392L;
    private final TrackedRace trackedRace;
    
    protected AbstractRankingMetric(TrackedRace trackedRace) {
        super();
        this.trackedRace = trackedRace;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    protected Duration getActualTimeSinceStartOfRace(Competitor competitor, TimePoint timePoint) {
        final Duration result;
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace == null) {
            result = null;
        } else {
            final Waypoint finish = getTrackedRace().getRace().getCourse().getLastWaypoint();
            if (finish == null) {
                result = null;
            } else {
                final MarkPassing finishingMarkPassing = getTrackedRace().getMarkPassing(competitor, finish);
                if (finishingMarkPassing != null) {
                    result = startOfRace.until(finishingMarkPassing.getTimePoint());
                } else {
                    result = startOfRace.until(timePoint);
                }
            }
        }
        return result;
    }
    
    protected Distance getWindwardDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        return getWindwardDistanceTraveled(competitor, getTrackedRace().getRace().getCourse().getFirstWaypoint(), timePoint);
    }
    
    /**
     * How far did <code>competitor</code> sail windwards/along-course since passing the <code>from</code> waypoint?
     */
    protected Distance getWindwardDistanceTraveled(Competitor competitor, Waypoint from, TimePoint timePoint) {
        TrackedLegOfCompetitor currentLeg = getTrackedRace().getCurrentLeg(competitor, timePoint);
        final Distance result;
        if (currentLeg == null || from == null) {
            result = null;
        } else {
            Distance d = Distance.NULL;
            boolean count = false; // start counting only once the "from" waypoint has been found
            for (TrackedLeg trackedLeg : getTrackedRace().getTrackedLegs()) {
                count = count || trackedLeg.getLeg().getFrom() == from;
                if (count) {
                    if (trackedLeg.getLeg() == currentLeg.getLeg()) {
                        // partial distance sailed:
                        d = d.add(trackedLeg.getWindwardDistanceFromLegStart(getTrackedRace().getTrack(competitor)
                                .getEstimatedPosition(timePoint, /* extrapolate */true)));
                        break;
                    } else {
                        d = d.add(trackedLeg.getWindwardDistance());
                    }
                }
            }
            result = d;
        }
        return result;
    }
}
