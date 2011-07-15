package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

/**
 * Provides a convenient view on the tracked leg, projecting to a single competitor's performance.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedLegOfCompetitorImpl implements TrackedLegOfCompetitor {
    private final TrackedLegImpl trackedLeg;
    private final Competitor competitor;
    
    public TrackedLegOfCompetitorImpl(TrackedLegImpl trackedLeg, Competitor competitor) {
        this.trackedLeg = trackedLeg;
        this.competitor = competitor;
    }

    protected TrackedLegImpl getTrackedLeg() {
        return trackedLeg;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public Leg getLeg() {
        return trackedLeg.getLeg();
    }
    
    private TrackedRace getTrackedRace() {
        return getTrackedLeg().getTrackedRace();
    }

    @Override
    public long getTimeInMilliSeconds(TimePoint timePoint) {
        long result = -1;
        MarkPassing passedEndWaypoint = getTrackedRace().getMarkPassing(getCompetitor(), getTrackedLeg().getLeg().getTo());
        if (passedEndWaypoint != null) {
            MarkPassing passedStartWaypoint = getTrackedRace().getMarkPassing(getCompetitor(), getTrackedLeg().getLeg().getFrom());
            if (passedStartWaypoint != null) {
                result = passedEndWaypoint.getTimePoint().asMillis() - passedStartWaypoint.getTimePoint().asMillis();
            } else {
                throw new RuntimeException(""+getCompetitor()+" passed waypoint at end of leg "+
                        getLeg()+" without having passed waypoint at beginning of leg");
            }
        }
        return result;
    }

    @Override
    public Distance getDistanceTraveled(TimePoint timePoint) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return Distance.NULL;
        } else {
            MarkPassing legEnd = getMarkPassingForLegEnd();
            TimePoint end = timePoint;
            if (legEnd != null && timePoint.compareTo(legEnd.getTimePoint()) > 0) {
                // timePoint is after leg finish; take leg end and end time point
                end = legEnd.getTimePoint();
            }
            return getTrackedRace().getTrack(getCompetitor()).getDistanceTraveled(legStart.getTimePoint(), end);
        }
    }

    private MarkPassing getMarkPassingForLegStart() {
        MarkPassing legStart = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getFrom());
        return legStart;
    }

    private MarkPassing getMarkPassingForLegEnd() {
        MarkPassing legEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
        return legEnd;
    }

    @Override
    public Speed getAverageSpeedOverGround(TimePoint timePoint) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return null;
        } else {
            TimePoint timePointToUse;
            if (hasFinishedLeg(timePoint)) {
                timePointToUse = getMarkPassingForLegEnd().getTimePoint();
            } else {
                // use time point of latest fix
                GPSFixMoving lastFix = getTrackedRace().getTrack(getCompetitor()).getLastRawFix();
                if (lastFix != null) {
                    timePointToUse = lastFix.getTimePoint();
                } else {
                    // No fix at all? Then we can't determine any speed 
                    return null;
                }
            }
            Distance d = getDistanceTraveled(timePointToUse);
            long millis = timePointToUse.asMillis() - legStart.getTimePoint().asMillis();
            return d.inTime(millis);
        }
    }

    @Override
    public Speed getMaximumSpeedOverGround(TimePoint timePoint) {
        // fetch all fixes on this leg so far and determine their maximum speed
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return null;
        }
        MarkPassing legEnd = getMarkPassingForLegEnd();
        TimePoint to;
        if (legEnd == null || legEnd.getTimePoint().compareTo(timePoint) >= 0) {
            to = timePoint;
        } else {
            to = legEnd.getTimePoint();
        }
        GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
        return track.getMaximumSpeedOverGround(legStart.getTimePoint(), to);
    }

    @Override
    public Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException {
        if (hasFinishedLeg(timePoint)) {
            return Distance.NULL;
        } else {
            Distance result = null;
            for (Buoy buoy : getLeg().getTo().getBuoys()) {
                Distance d = getWindwardDistanceTo(buoy, timePoint);
                if (result == null || d.compareTo(result) < 0) {
                    result = d;
                }
            }
            return result;
        }
    }

    /**
     * If the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees collinear with the
     * wind's bearing, the competitor's position is projected onto the line crossing <code>buoy</code> in the wind's
     * bearing, and the distance from the projection to the <code>buoy</code> is returned. Otherwise, it is assumed that
     * the leg is neither an upwind nor a downwind leg, and hence the true distance to <code>buoy</code> is returned.
     */
    private Distance getWindwardDistanceTo(Buoy buoy, TimePoint at) throws NoWindException {
        Position estimatedPosition = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at, false);
        if (!hasStartedLeg(at) || estimatedPosition == null) {
            // covers the case with no fixes for this leg yet, also if the mark passing has already been received
            estimatedPosition = getTrackedRace().getTrack(getLeg().getFrom().getBuoys().iterator().next())
                    .getEstimatedPosition(at, false);
        }
        if (estimatedPosition == null) { // may happen if mark positions haven't been received yet
            return null;
        }
        return getWindwardDistance(estimatedPosition, getTrackedRace().getTrack(buoy).getEstimatedPosition(at, false),
                at);
    }

    /**
     * If the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees collinear with the
     * wind's bearing, the competitor's position is projected onto the line crossing <code>buoy</code> in the wind's
     * bearing, and the distance from the projection to the <code>buoy</code> is returned. Otherwise, it is assumed that
     * the leg is neither an upwind nor a downwind leg, and hence the true distance to <code>buoy</code> is returned.
     * 
     * @param at the wind estimation is performed for this point in time
     */
    private Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at) throws NoWindException {
        if (getTrackedLeg().isUpOrDownwindLeg(at)) {
            Wind wind = getWind(pos1.translateGreatCircle(pos1.getBearingGreatCircle(pos2), pos1.getDistance(pos2).scale(0.5)), at);
            Position projectionToLineThroughPos2 = pos1.projectToLineThrough(pos2, wind.getBearing());
            return projectionToLineThroughPos2.getDistance(pos2);
        } else {
            // cross leg, return true distance
            return pos1.getDistance(pos2);
        }
    }
    
    /**
     * Projects <code>speed</code> onto the wind direction to see how fast a boat travels
     * "along the wind's direction."
     * 
     * @throws NoWindException in case the wind direction is not known
     */
    private SpeedWithBearing getWindwardSpeed(SpeedWithBearing speed, TimePoint at) throws NoWindException {
        Wind wind = getWind(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at, false), at);
        if (wind == null) {
            throw new NoWindException("Need at least wind direction to determine windward speed");
        }
        Bearing bearing = wind.getBearing();
        double cos = Math.cos(speed.getBearing().getRadians()-wind.getBearing().getRadians());
        if (cos < 0) {
            bearing = bearing.reverse();
        }
        SpeedWithBearing result = new KnotSpeedWithBearingImpl(Math.abs(wind.getKnots() * cos), bearing);
        return result;
    }

    /**
     * For now, we have an incredibly simple wind "model" which assigns a single common wind force and bearing
     * to all positions on the course, only variable over time.
     */
    private Wind getWind(Position p, TimePoint at) {
        return getTrackedRace().getWind(p, at);
    }

    @Override
    public int getRank(TimePoint timePoint) {
        List<TrackedLegOfCompetitor> competitorTracksByRank = getTrackedLeg().getCompetitorTracksOrderedByRank(timePoint);
        return competitorTracksByRank.indexOf(this)+1;
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        Speed result = null;
        MarkPassing start = getMarkPassingForLegStart();
        if (start != null && start.getTimePoint().compareTo(timePoint) <= 0) {
            MarkPassing end = getMarkPassingForLegEnd();
            if (end != null) {
                TimePoint to;
                if (timePoint.compareTo(end.getTimePoint()) >= 0) {
                    to = end.getTimePoint();
                } else {
                    to = timePoint;
                }
                Position endPos = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(to, /* extrapolate */ false);
                if (endPos != null) {
                    Distance d = getWindwardDistance(
                            getTrackedRace().getTrack(getCompetitor())
                                    .getEstimatedPosition(start.getTimePoint(), false), endPos, to);
                    result = d.inTime(to.asMillis() - start.getTimePoint().asMillis());
                }
            }
        }
        return result;
    }

    @Override
    public int getNumberOfTacks(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfJibes(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfDirectionChanges(TimePoint timePoint) {
        return getNumberOfTacks(timePoint)+getNumberOfJibes(timePoint);
    }
    
    @Override
    public Distance getWindwardDistanceToOverallLeader(TimePoint timePoint) throws NoWindException {
        Competitor leader = getTrackedLeg().getRanks(timePoint).keySet().iterator().next();
        TrackedLegOfCompetitor leaderLeg = getTrackedRace().getCurrentLeg(leader, timePoint);
        if (leaderLeg.getLeg() == getLeg()) {
            // we're still in the same leg with leader; compute windward distance to leader
            return getWindwardDistance(getTrackedRace().getTrack(leader).getEstimatedPosition(timePoint, /* extrapolate */ false),
                    getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(timePoint, /* extrapolate */ false), timePoint);
        } else {
            return null;
            // TODO special case leader has finished race already
        }
    }

    @Override
    public Double getGapToLeaderInSeconds(TimePoint timePoint) throws NoWindException {
        // If the leader already completed this leg, compute the estimated arrival time at the
        // end of this leg; if this leg's competitor also already finished the leg, return the
        // difference between this competitor's leg completion time point and the leader's completion
        // time point; else, calculate the windward distance to the leader and divide by
        // the windward speed
        Speed windwardSpeed = getWindwardSpeed(getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(timePoint), timePoint);
        Iterator<MarkPassing> markPassingsForLegEnd = getTrackedRace().getMarkPassingsInOrder(getLeg().getTo()).iterator();
        // Has our competitor started the leg already? If not, we won't be able to compute a gap
        if (hasStartedLeg(timePoint)) {
            if (markPassingsForLegEnd.hasNext()) {
                // someone has already finished the leg
                TimePoint whenLeaderFinishedLeg = markPassingsForLegEnd.next().getTimePoint();
                // Was it before the requested timePoint?
                if (whenLeaderFinishedLeg.compareTo(timePoint) <= 0) {
                    // Has our competitor also already finished this leg?
                    if (hasFinishedLeg(timePoint)) {
                        // Yes, so the gap is the time period between the time points at which the leader and
                        // our competitor finished this leg.
                        return (getMarkPassingForLegEnd().getTimePoint().asMillis() - whenLeaderFinishedLeg.asMillis()) / 1000.;
                    } else {
                        // leader has finished already; our competitor hasn't
                        Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint);
                        long millisSinceLeaderPassedMarkToTimePoint = timePoint.asMillis()
                                - whenLeaderFinishedLeg.asMillis();
                        return windwardDistanceToGo.getMeters() / windwardSpeed.getMetersPerSecond()
                                + millisSinceLeaderPassedMarkToTimePoint / 1000.;
                    }
                }
            }
            // no-one has finished this leg yet at timePoint
            Competitor leader = getTrackedLeg().getLeader(timePoint);
            // Maybe our competitor is the leader. Check:
            if (leader == getCompetitor()) {
                return 0.0; // the leader's gap to the leader
            } else {
                // no, we're not the leader, so compute our windward distance and divide by our current VMG
                Position ourEstimatedPosition = getTrackedRace().getTrack(getCompetitor())
                        .getEstimatedPosition(timePoint, false);
                Position leaderEstimatedPosition = getTrackedRace().getTrack(leader)
                        .getEstimatedPosition(timePoint, false);
                if (ourEstimatedPosition == null || leaderEstimatedPosition == null) {
                    return null;
                } else {
                    Distance windwardDistanceToGo = getWindwardDistance(ourEstimatedPosition, leaderEstimatedPosition,
                            timePoint);
                    return windwardDistanceToGo.getMeters() / windwardSpeed.getMetersPerSecond();
                }
            }
        } else {
            // our competitor hasn't started the leg yet, so we can't compute a gap since we don't
            // have a speed estimate
            return null;
        }
    }

    @Override
    public boolean hasStartedLeg(TimePoint timePoint) {
        MarkPassing markPassingForLegStart = getMarkPassingForLegStart();
        return markPassingForLegStart != null && markPassingForLegStart.getTimePoint().compareTo(timePoint) <= 0;
    }

    @Override
    public boolean hasFinishedLeg(TimePoint timePoint) {
        MarkPassing markPassingForLegEnd = getMarkPassingForLegEnd();
        return markPassingForLegEnd != null && markPassingForLegEnd.getTimePoint().compareTo(timePoint) <= 0;
    }

    @Override
    public Speed getVelocityMadeGood(TimePoint at) throws NoWindException {
        if (hasStartedLeg(at)) {
            return getWindwardSpeed(getSpeedOverGround(at), at);
        } else {
            return null;
        }
    }

    @Override
    public SpeedWithBearing getSpeedOverGround(TimePoint at) {
        if (hasStartedLeg(at)) {
            return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(at);
        } else {
            return null;
        }
    }

    @Override
    public Double getEstimatedTimeToNextMarkInSeconds(TimePoint timePoint) throws NoWindException {
        Double result;
        if (hasFinishedLeg(timePoint)) {
            result = 0.0;
        } else {
            if (hasStartedLeg(timePoint)) {
                Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint);
                Speed vmg = getVelocityMadeGood(timePoint);
                result = windwardDistanceToGo.getMeters() / vmg.getMetersPerSecond();
            } else {
                result = null;
            }
        }
        return result;
    }

}
