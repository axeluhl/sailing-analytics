package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
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
    
    /**
     * Predicts how long <code>who</code> will take to reach competitor <code>to</code>'s position at
     * <code>timePoint</code>, starting at <code>who</code>'s position at <code>timePoint</code>, assuming a continued
     * performance for <code>who</code> that matches her average VMG on her current leg so far, and equal performance
     * with <code>to</code> on any subsequent leg that <code>who</code> needs to travel to reach <code>to</code>'s
     * position at <code>timePoint</code>. If <code>to</code> has already finished the race, the finish line position
     * is where <code>who</code> needs to arrive.
     * <p>
     * If <code>to</code> is already in a later leg, <code>who</code>'s remaining duration to reach the end of her
     * current leg is estimated using
     * {@link TrackedLegOfCompetitor#getEstimatedTimeToNextMark(TimePoint, com.sap.sailing.domain.tracking.WindPositionMode)}
     * ; then from the waypoint reached this way the
     * {@link #getWindwardDistanceTraveled(Competitor, Waypoint, TimePoint) windward distance to competitor
     * <code>to</code>} is determined and from this, using the handicaps for both competitors, <code>who</code> and
     * <code>to</code>, their performance between the waypoint and <code>to</code>'s position at <code>timePoint</code>
     * is equated, hence assuming that considering their handicaps, both competitors are doing equally well on this part
     * of the course, meaning that <code>who</code> will not gain any time on <code>to</code> during this period. From
     * the equations, the duration it will take <code>who</code> to reach this position starting at the upcoming
     * waypoint can be determined which is then added to the duration estimated to reach that upcoming waypoint (based
     * on <code>who</code>'s average VMG in her current leg).
     * <p>
     * 
     * If <code>who</code> and <code>to</code> are in the same leg, the windward distance is calculated, and
     * <code>who</code>'s average VMG during the current leg is used to estimate the time until she reaches the position
     * <code>to</code> had at <code>timePoint</code>.
     * 
     * Precondition: <code>who</code>'s windward / along-course position is behind that of <code>to</code>, or an
     * {@link IllegalArgumentException} will be thrown.<p>
     * 
     * @return <code>null</code>, if either of the two competitors' current legs is null
     */
    protected Duration getPredictedDurationToReachWindwardPositionOf(Competitor who, Competitor to, TimePoint timePoint) {
        final TrackedLegOfCompetitor currentLegWho = getTrackedRace().getCurrentLeg(who, timePoint);
        final TrackedLegOfCompetitor currentLegTo = getTrackedRace().getCurrentLeg(to, timePoint);
        final Duration result;
        if (who == to) { // the same competitor requires no time to reach its own position; it's already there...
            result = Duration.NULL;
        } else if (currentLegWho == null || currentLegTo == null) {
            result = null;
        } else {
            final Position windwardPositionToReachInWhosCurrentLeg =
                    (currentLegWho.getLeg() == currentLegTo.getLeg())
                            // both are currently in the same leg; estimate who's arrival at to's current windward position
                            // using who's average VMG in current leg
                            ? getTrackedRace().getTrack(to).getEstimatedPosition(timePoint, /* extrapolate */ true)
                            // not in the same leg; let "who" travel to the end of the leg
                            : getTrackedRace().getApproximatePosition(currentLegWho.getLeg().getTo(), timePoint);
            final Duration toEndOfLegOrTo = currentLegWho.getAverageVelocityMadeGood(timePoint).getDuration(
                    currentLegWho.getTrackedLeg().getWindwardDistance(
                            getTrackedRace().getTrack(who).getEstimatedPosition(timePoint, /* extrapolate */true),
                            windwardPositionToReachInWhosCurrentLeg, timePoint, WindPositionMode.LEG_MIDDLE));
            final Duration durationForSubsequentLegsToReachAtEqualPerformance = getDurationToReachAtEqualPerformance(who, to,
                    currentLegWho.getLeg().getTo(), timePoint.plus(toEndOfLegOrTo), timePoint);
            result = toEndOfLegOrTo.plus(durationForSubsequentLegsToReachAtEqualPerformance);
        }
        return result;
    }
    
    /**
     * Computes the duration that <code>who</code> would take to reach <code>to</code>'s windward / along-track position
     * at <code>timePoint</code>, starting at <code>fromWaypoint</code>, assuming the same corrected performance at
     * which <code>to</code> sailed starting at <code>fromWaypoint</code> up to her current position.
     * 
     * @param whenWhoIsAtFromWaypoint the time point when <code>who</code> is expected to be at <code>fromWaypoint</code>
     */
    protected abstract Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint whenWhoIsAtFromWaypoint, TimePoint timePointOfTosPosition);

    protected Distance getWindwardDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        return getWindwardDistanceTraveled(competitor, getTrackedRace().getRace().getCourse().getFirstWaypoint(), timePoint);
    }
    
    /**
     * How far did <code>competitor</code> sail windwards/along-course since passing the <code>from</code> waypoint?
     * 
     * @param timePoint needed to determine <code>competior</code>'s position at that time point; note that the
     * time point for wind approximation is taken to be a reference time point selected based on the mark passings
     * for the respective leg's from/to waypoints.
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
