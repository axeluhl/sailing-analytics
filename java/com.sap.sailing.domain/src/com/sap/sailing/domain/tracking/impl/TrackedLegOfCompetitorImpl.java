package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
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
    private static final long serialVersionUID = -7060076837717432808L;
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
    public Long getTimeInMilliSeconds(TimePoint timePoint) {
        long result = 0;
        MarkPassing passedStartWaypoint = getTrackedRace().getMarkPassing(getCompetitor(),
                getTrackedLeg().getLeg().getFrom());
        if (passedStartWaypoint != null) {
            MarkPassing passedEndWaypoint = getTrackedRace().getMarkPassing(getCompetitor(),
                    getTrackedLeg().getLeg().getTo());
            if (passedEndWaypoint != null) {
                result = passedEndWaypoint.getTimePoint().asMillis() - passedStartWaypoint.getTimePoint().asMillis();
            } else {
                result = timePoint.asMillis() - passedStartWaypoint.getTimePoint().asMillis();
            }
        }
        return result;
    }

    @Override
    public Distance getDistanceTraveled(TimePoint timePoint) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return null;
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
        Speed result;
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            result = null;
        } else {
            TimePoint timePointToUse;
            if (hasFinishedLeg(timePoint)) {
                timePointToUse = getMarkPassingForLegEnd().getTimePoint();
            } else {
                // use time point of latest fix if before timePoint, otherwise timePoint
                GPSFixMoving lastFix = getTrackedRace().getTrack(getCompetitor()).getLastRawFix();
                if (lastFix == null) {
                    // No fix at all? Then we can't determine any speed 
                    timePointToUse = null;
                } else if (lastFix.getTimePoint().compareTo(timePoint) < 0) {
                    timePointToUse = lastFix.getTimePoint();
                } else {
                    timePointToUse = timePoint;
                }
            }
            if (timePointToUse != null) {
                Distance d = getDistanceTraveled(timePointToUse);
                long millis = timePointToUse.asMillis() - legStart.getTimePoint().asMillis();
                result = d.inTime(millis);
            } else {
                result = null;
            }
        }
        return result;
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
                if (result == null || d != null && d.compareTo(result) < 0) {
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
            estimatedPosition = getTrackedRace().getOrCreateTrack(getLeg().getFrom().getBuoys().iterator().next())
                    .getEstimatedPosition(at, false);
        }
        if (estimatedPosition == null) { // may happen if mark positions haven't been received yet
            return null;
        }
        return getWindwardDistance(estimatedPosition, getTrackedRace().getOrCreateTrack(buoy).getEstimatedPosition(at, false),
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
    @Override
    public Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at) throws NoWindException {
        if (getTrackedLeg().isUpOrDownwindLeg(at)) {
            Wind wind = getWind(pos1.translateGreatCircle(pos1.getBearingGreatCircle(pos2), pos1.getDistance(pos2).scale(0.5)), at);
            if (wind == null) {
                return pos2.alongTrackDistance(pos1, getTrackedLeg().getLegBearing(at));
            } else {
                Position projectionToLineThroughPos2 = pos1.projectToLineThrough(pos2, wind.getBearing());
                return projectionToLineThroughPos2.getDistance(pos2);
            }
        } else {
            // reaching leg, return distance projected onto leg's bearing
            return pos2.alongTrackDistance(pos1, getTrackedLeg().getLegBearing(at));
        }
    }
    
    /**
     * Projects <code>speed</code> onto the wind direction for upwind/downwind legs to see how fast a boat travels
     * "along the wind's direction." For reaching legs (neither upwind nor downwind), the speed is projected onto
     * the leg's direction.
     * 
     * @throws NoWindException in case the wind direction is not known
     */
    private SpeedWithBearing getWindwardSpeed(SpeedWithBearing speed, TimePoint at) throws NoWindException {
        SpeedWithBearing result = null;
        if (speed != null) {
            Bearing projectToBearing;
            if (getTrackedLeg().isUpOrDownwindLeg(at)) {
                Wind wind = getWind(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at, false), at);
                if (wind == null) {
                    throw new NoWindException("Need at least wind direction to determine windward speed");
                }
                projectToBearing = wind.getBearing();
            } else {
                projectToBearing = getTrackedLeg().getLegBearing(at);
            }
            double cos = Math.cos(speed.getBearing().getRadians() - projectToBearing.getRadians());
            if (cos < 0) {
                projectToBearing = projectToBearing.reverse();
            }
            result = new KnotSpeedWithBearingImpl(Math.abs(speed.getKnots() * cos), projectToBearing);
        }
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
        int result = 0;
        if (hasStartedLeg(timePoint)) {
            List<TrackedLegOfCompetitor> competitorTracksByRank = getTrackedLeg().getCompetitorTracksOrderedByRank(timePoint);
            result = competitorTracksByRank.indexOf(this)+1;
        }
        return result;
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
    public Integer getNumberOfTacks(TimePoint timePoint) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, /* waitForLatest */ true);
            result = 0;
            for (Maneuver maneuver : maneuvers) {
                if (maneuver.getType() == ManeuverType.TACK) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public List<Maneuver> getManeuvers(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        MarkPassing legEnd = getMarkPassingForLegEnd();
        TimePoint end = timePoint;
        if (legEnd != null && timePoint.compareTo(legEnd.getTimePoint()) > 0) {
            // timePoint is after leg finish; take leg end and end time point
            end = legEnd.getTimePoint();
        }
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(getCompetitor(),
                getMarkPassingForLegStart().getTimePoint(), end, waitForLatest);
        return maneuvers;
    }

    @Override
    public Integer getNumberOfJibes(TimePoint timePoint) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, /* waitForLatest */ true);
            result = 0;
            for (Maneuver maneuver : maneuvers) {
                if (maneuver.getType() == ManeuverType.JIBE) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public Integer getNumberOfPenaltyCircles(TimePoint timePoint) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, /* waitForLatest */ true);
            result = 0;
            for (Maneuver maneuver : maneuvers) {
                if (maneuver.getType() == ManeuverType.PENALTY_CIRCLE) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public Distance getWindwardDistanceToOverallLeader(TimePoint timePoint) throws NoWindException {
        // FIXME bug 607 it seems the following fetches the leader of this leg, not the overall leader; validate!!! Use getTrackedRace().getRanks() instead
        Competitor leader = getTrackedRace().getOverallLeader(timePoint);
        TrackedLegOfCompetitor leaderLeg = getTrackedRace().getCurrentLeg(leader, timePoint);
        Distance result = null;
        Position leaderPosition = getTrackedRace().getTrack(leader).getEstimatedPosition(timePoint, /* extrapolate */ false);
        Position currentPosition = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(timePoint, /* extrapolate */ false);
        if (leaderPosition != null && currentPosition != null) {
            result = Distance.NULL;
            boolean foundCompetitorsLeg = false;
            for (Leg leg : getTrackedRace().getRace().getCourse().getLegs()) {
                if (leg == getLeg()) {
                    foundCompetitorsLeg = true;
                }
                if (foundCompetitorsLeg) {
                    // if the leaderLeg is null, the leader has already arrived
                    if (leaderLeg == null || leg != leaderLeg.getLeg()) {
                        // add distance to next mark
                        Position nextMarkPosition = getTrackedRace().getApproximatePosition(leg.getTo(), timePoint);
                        Distance distanceToNextMark = getTrackedRace().getTrackedLeg(getCompetitor(), leg)
                                .getWindwardDistance(currentPosition, nextMarkPosition, timePoint);
                        result = new MeterDistance(result.getMeters() + distanceToNextMark.getMeters());
                        currentPosition = nextMarkPosition;
                    } else {
                        // we're now in the same leg with leader; compute windward distance to leader
                        result = new MeterDistance(result.getMeters()
                                + getTrackedRace().getTrackedLeg(getCompetitor(), leg)
                                        .getWindwardDistance(currentPosition, leaderPosition, timePoint).getMeters());
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Distance getAverageCrossTrackError(TimePoint timePoint, boolean waitForLatestAnalysis) throws NoWindException {
        Distance result = null;
        final MarkPassing legStartMarkPassing = getTrackedRace().getMarkPassing(competitor, getLeg().getFrom());
        if (legStartMarkPassing != null) {
            TimePoint legStart = legStartMarkPassing.getTimePoint();
            final MarkPassing legEndMarkPassing = getTrackedRace().getMarkPassing(competitor, getLeg().getTo());
            TimePoint to;
            if (legEndMarkPassing == null || legEndMarkPassing.getTimePoint().compareTo(timePoint) > 0) {
                to = timePoint;
            } else {
                to = legEndMarkPassing.getTimePoint();
            }
            result = getTrackedRace().getAverageCrossTrackError(competitor, legStart, to, /* upwindOnly */ false, waitForLatestAnalysis);
        }
        return result;
    }

    @Override
    public Double getGapToLeaderInSeconds(TimePoint timePoint, final Competitor leaderInLegAtTimePoint)
            throws NoWindException {
        return getGapToLeaderInSeconds(timePoint, new LeaderGetter() {
            @Override
            public Competitor getLeader() {
                return leaderInLegAtTimePoint;
            }
        });
    }

    private static interface LeaderGetter {
        Competitor getLeader();
    }
    
    @Override
    public Double getGapToLeaderInSeconds(final TimePoint timePoint) throws NoWindException {
        return getGapToLeaderInSeconds(timePoint, new LeaderGetter() {
            @Override
            public Competitor getLeader() {
                return getTrackedLeg().getLeader(timePoint);
            }
        });
    }
    
    private Double getGapToLeaderInSeconds(TimePoint timePoint, LeaderGetter leaderGetter) throws NoWindException {
        // If the leader already completed this leg, compute the estimated arrival time at the
        // end of this leg; if this leg's competitor also already finished the leg, return the
        // difference between this competitor's leg completion time point and the leader's completion
        // time point; else, calculate the windward distance to the leader and divide by
        // the windward speed
        Speed windwardSpeed = getWindwardSpeed(getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(timePoint), timePoint);
        Double result = null;
        // Has our competitor started the leg already? If not, we won't be able to compute a gap
        if (hasStartedLeg(timePoint)) {
            Iterable<MarkPassing> markPassingsInOrder = getTrackedRace().getMarkPassingsInOrder(getLeg().getTo());
            if (markPassingsInOrder != null) {
                MarkPassing firstMarkPassing = null;
                synchronized (markPassingsInOrder) {
                    Iterator<MarkPassing> markPassingsForLegEnd = markPassingsInOrder.iterator();
                    if (markPassingsForLegEnd.hasNext()) {
                        firstMarkPassing = markPassingsForLegEnd.next();
                    }
                }
                if (firstMarkPassing != null) {
                    // someone has already finished the leg
                    TimePoint whenLeaderFinishedLeg = firstMarkPassing.getTimePoint();
                    // Was it before the requested timePoint?
                    if (whenLeaderFinishedLeg.compareTo(timePoint) <= 0) {
                        // Has our competitor also already finished this leg?
                        if (hasFinishedLeg(timePoint)) {
                            // Yes, so the gap is the time period between the time points at which the leader and
                            // our competitor finished this leg.
                            return (getMarkPassingForLegEnd().getTimePoint().asMillis() - whenLeaderFinishedLeg
                                    .asMillis()) / 1000.;
                        } else {
                            if (windwardSpeed == null) {
                                return null;
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
                }
                // no-one has finished this leg yet at timePoint
                Competitor leader = leaderGetter.getLeader();
                // Maybe our competitor is the leader. Check:
                if (leader == getCompetitor()) {
                    return 0.0; // the leader's gap to the leader
                } else {
                    if (windwardSpeed == null) {
                        return null;
                    } else {
                        // no, we're not the leader, so compute our windward distance and divide by our current VMG
                        Position ourEstimatedPosition = getTrackedRace().getTrack(getCompetitor())
                                .getEstimatedPosition(timePoint, false);
                        Position leaderEstimatedPosition = getTrackedRace().getTrack(leader).getEstimatedPosition(
                                timePoint, false);
                        if (ourEstimatedPosition == null || leaderEstimatedPosition == null) {
                            return null;
                        } else {
                            Distance windwardDistanceToGo = getWindwardDistance(ourEstimatedPosition,
                                    leaderEstimatedPosition, timePoint);
                            return windwardDistanceToGo.getMeters() / windwardSpeed.getMetersPerSecond();
                        }
                    }
                }
            }
        }
        // else our competitor hasn't started the leg yet, so we can't compute a gap since we don't
        // have a speed estimate; leave result == null
        return result;
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
    public TimePoint getStartTime() {
        MarkPassing markPassingForLegStart = getMarkPassingForLegStart();
        return markPassingForLegStart == null ? null : markPassingForLegStart.getTimePoint();
    }

    @Override
    public TimePoint getFinishTime() {
        MarkPassing markPassingForLegEnd = getMarkPassingForLegEnd();
        return markPassingForLegEnd == null ? null : markPassingForLegEnd.getTimePoint();
    }

    @Override
    public Speed getVelocityMadeGood(TimePoint at) throws NoWindException {
        if (hasStartedLeg(at)) {
            TimePoint timePoint;
            if (hasFinishedLeg(at)) {
                // use the leg finishing time point
                timePoint = getMarkPassingForLegEnd().getTimePoint();
            } else {
                timePoint = at;
            }
            SpeedWithBearing speedOverGround = getSpeedOverGround(timePoint);
            return speedOverGround == null ? null : getWindwardSpeed(speedOverGround, timePoint);
        } else {
            return null;
        }
    }

    @Override
    public SpeedWithBearing getSpeedOverGround(TimePoint at) {
        if (hasStartedLeg(at)) {
            TimePoint timePoint;
            if (hasFinishedLeg(at)) {
                // use the leg finishing time point
                timePoint = getMarkPassingForLegEnd().getTimePoint();
            } else {
                timePoint = at;
            }
            return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(timePoint);
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
                result = vmg == null ? null : windwardDistanceToGo.getMeters() / vmg.getMetersPerSecond();
            } else {
                result = null;
            }
        }
        return result;
    }

}
