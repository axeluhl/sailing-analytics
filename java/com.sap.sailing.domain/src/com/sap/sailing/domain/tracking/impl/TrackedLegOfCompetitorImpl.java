package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.util.impl.ArrayListNavigableSet;

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

    @Override
    public TrackedLegImpl getTrackedLeg() {
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
    public TimePoint getTimePointNotAfterFinishingOfLeg(TimePoint timePoint) {
        final TimePoint result;
        MarkPassing passedStartWaypoint = getTrackedRace().getMarkPassing(getCompetitor(),
                getTrackedLeg().getLeg().getFrom());
        if (passedStartWaypoint != null && !passedStartWaypoint.getTimePoint().after(timePoint)) {
            MarkPassing passedEndWaypoint = getMarkPassingForLegEnd();
            if (passedEndWaypoint != null && timePoint.after(passedEndWaypoint.getTimePoint())) {
                // the query asks for a time point after the competitor has finished the leg; return the total leg time
                result = passedEndWaypoint.getTimePoint();
            } else {
                if (getTrackedRace().getEndOfTracking() != null && timePoint.after(getTrackedRace().getEndOfTracking())) {
                    result = getTrackedRace().getEndOfTracking();
                } else {
                    result = timePoint;
                }
            }
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public Duration getTime(TimePoint timePoint) {
        final Duration result;
        MarkPassing passedStartWaypoint = getMarkPassingForLegStart();
        if (passedStartWaypoint == null) {
            result = null;
        } else {
            final TimePoint timePointNotAfterFinishingOfLeg = getTimePointNotAfterFinishingOfLeg(timePoint);
            result = timePointNotAfterFinishingOfLeg == null ? null : passedStartWaypoint.getTimePoint().until(timePoint);
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
    
    @Override
    public Distance getDistanceTraveledConsideringGateStart(TimePoint timePoint) {
        final Distance result;
        final Distance preResult = getDistanceTraveled(timePoint);
        final Waypoint from = getLeg().getFrom();
        if (preResult != null && from == getTrackedRace().getRace().getCourse().getFirstWaypoint()) {
            result = preResult.add(getTrackedRace().getAdditionalGateStartDistance(getCompetitor(), timePoint));
        } else {
            result = preResult;
        }
        return result;
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
    public Util.Pair<GPSFixMoving, Speed> getMaximumSpeedOverGround(TimePoint timePoint) {
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
    public Distance getWindwardDistanceToGo(TimePoint timePoint, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        if (hasFinishedLeg(timePoint)) {
            return Distance.NULL;
        } else {
            Distance result = null;
            for (Mark mark : getLeg().getTo().getMarks()) {
                Distance d = getWindwardDistanceTo(mark, timePoint, windPositionMode, cache);
                if (result == null || d != null && d.compareTo(result) < 0) {
                    result = d;
                }
            }
            return result;
        }
    }

    @Override
    public Distance getWindwardDistanceToGo(TimePoint timePoint, WindPositionMode windPositionMode) {
        return getWindwardDistanceToGo(timePoint, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }

    /**
     * If the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees collinear with the
     * wind's bearing, the competitor's position is projected onto the line crossing <code>mark</code> in the wind's
     * bearing, and the distance from the projection to the <code>mark</code> is returned. Otherwise, it is assumed that
     * the leg is neither an upwind nor a downwind leg, and hence the true distance to <code>mark</code> is returned. A
     * cache for wind and leg type / bearing can be passed to avoid their redundant calculation during a single
     * round-trip.<p>
     * 
     * If no wind information is available, again the true geometrical distance to <code>mark</code> is returned.
     */
    private Distance getWindwardDistanceTo(Mark mark, TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        Position estimatedPosition = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at, false);
        if (!hasStartedLeg(at) || estimatedPosition == null) {
            // covers the case with no fixes for this leg yet, also if the mark passing has already been received
            estimatedPosition = getTrackedRace().getOrCreateTrack(getLeg().getFrom().getMarks().iterator().next())
                    .getEstimatedPosition(at, false);
        }
        if (estimatedPosition == null) { // may happen if mark positions haven't been received yet
            return null;
        }
        final Position estimatedMarkPosition = getTrackedRace().getOrCreateTrack(mark).getEstimatedPosition(at, false);
        if (estimatedMarkPosition == null) {
            return null;
        }
        return getTrackedLeg().getAbsoluteWindwardDistance(estimatedPosition, estimatedMarkPosition, at, windPositionMode, cache);
    }

    /**
     * Projects <code>speed</code> onto the wind direction for upwind/downwind legs to see how fast a boat travels
     * "along the wind's direction." For reaching legs (neither upwind nor downwind), the speed is projected onto
     * the leg's direction.
     * @param windPositionMode see {@link #getWind(Position, TimePoint, Set)}
     * 
     * @throws NoWindException in case the wind direction is not known
     */
    private SpeedWithBearing getWindwardSpeed(SpeedWithBearing speed, final TimePoint at, WindPositionMode windPositionMode,
            WindLegTypeAndLegBearingCache cache) {
        SpeedWithBearing result = null;
        if (speed != null) {
            Bearing projectToBearing;
            try {
                if (cache.getLegType(getTrackedLeg(), at) != LegType.REACHING) {
                    final Wind wind;
                    if (windPositionMode == WindPositionMode.EXACT) {
                        wind = cache.getWind(getTrackedRace(), getCompetitor(), at);
                    } else {
                        wind = getTrackedRace().getWind(
                                getTrackedLeg().getEffectiveWindPosition(
                                        () -> getTrackedRace().getTrack(getCompetitor())
                                                .getEstimatedPosition(at, false), at, windPositionMode), at);
                    }
                    if (wind == null) {
                        throw new NoWindException("Need at least wind direction to determine windward speed");
                    }
                    projectToBearing = wind.getBearing();
                } else {
                    projectToBearing = cache.getLegBearing(getTrackedLeg(), at);
                }
            } catch (NoWindException nwe) {
                // as fallback in the absence of wind information, project to leg bearing
                projectToBearing = cache.getLegBearing(getTrackedLeg(), at);
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
     * For now, we have an incredibly simple wind "model" which assigns a single common wind force and bearing to all
     * positions on the course, only variable over time.
     * 
     * @param windPositionMode
     *            For {@link WindPositionMode#EXACT}, the wind at position <code>p</code> is determined. For type
     *            {@link WindPositionMode#LEG_MIDDLE}, the {@link TrackedLeg#getMiddleOfLeg middle of the tracked leg}
     *            is determined and used as position. If the mode is {@link WindPositionMode#GLOBAL_AVERAGE}, <code>null</code>
     *            is passed as position to {@link TrackedRace#getWind(Position, TimePoint)} which yields a general average
     *            of the various wind sources available for the race, independent of any position.
     */
    @Override
    public int getRank(TimePoint timePoint) {
        return getRank(timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    @Override
    public int getRank(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        int result = 0;
        if (hasStartedLeg(timePoint)) {
            List<TrackedLegOfCompetitor> competitorTracksByRank = getTrackedLeg().getCompetitorTracksOrderedByRank(timePoint, cache);
            result = competitorTracksByRank.indexOf(this)+1;
        }
        return result;
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint) {
        return getAverageVelocityMadeGood(timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Speed result = null;
        MarkPassing start = getMarkPassingForLegStart();
        if (start != null && start.getTimePoint().compareTo(timePoint) <= 0) {
            MarkPassing end = getMarkPassingForLegEnd();
            final TimePoint to;
            if (end != null && timePoint.compareTo(end.getTimePoint()) >= 0) {
                to = end.getTimePoint();
            } else {
                to = timePoint;
            }
            final Position endPos = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(to, /* extrapolate */false);
            if (endPos != null) {
                Distance d = getTrackedLeg().getAbsoluteWindwardDistance(
                        getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(start.getTimePoint(), false),
                        endPos, to, WindPositionMode.EXACT, cache);
                result = d.inTime(to.asMillis() - start.getTimePoint().asMillis());
            }
        }
        return result;
    }

    
    @Override
    public Integer getNumberOfTacks(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
    public Integer getNumberOfJibes(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
    public Integer getNumberOfPenaltyCircles(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            List<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
    public Distance getWindwardDistanceToOverallLeader(TimePoint timePoint, WindPositionMode windPositionMode) {
        return getWindwardDistanceToOverallLeader(timePoint, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    @Override
    public Distance getWindwardDistanceToOverallLeader(TimePoint timePoint, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        // FIXME bug 607 it seems the following fetches the leader of this leg, not the overall leader; validate!!! Use getTrackedRace().getRanks() instead
        Competitor leader = getTrackedRace().getOverallLeader(timePoint);
        TrackedLegOfCompetitor leaderLeg = getTrackedRace().getCurrentLeg(leader, timePoint);
        Distance result = null;
        Position leaderPosition = getTrackedRace().getTrack(leader).getEstimatedPosition(timePoint, /* extrapolate */ false);
        Position currentPosition = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(timePoint, /* extrapolate */ false);
        if (leaderPosition != null && currentPosition != null) {
            result = Distance.NULL;
            boolean foundCompetitorsLeg = false;
            getTrackedRace().getRace().getCourse().lockForRead();
            try {
                for (Leg leg : getTrackedRace().getRace().getCourse().getLegs()) {
                    if (leg == getLeg()) {
                        foundCompetitorsLeg = true;
                    }
                    if (foundCompetitorsLeg) {
                        // if the leaderLeg is null, the leader has already arrived
                        if (leaderLeg == null || leg != leaderLeg.getLeg()) {
                            // add distance to next mark
                            Position nextMarkPosition = getTrackedRace().getApproximatePosition(leg.getTo(), timePoint);
                            Distance distanceToNextMark = getTrackedRace().getTrackedLeg(leg)
                                    .getAbsoluteWindwardDistance(currentPosition, nextMarkPosition, timePoint, windPositionMode, cache);
                            result = new MeterDistance(result.getMeters() + distanceToNextMark.getMeters());
                            currentPosition = nextMarkPosition;
                        } else {
                            // we're now in the same leg with leader; compute windward distance to leader
                            result = new MeterDistance(result.getMeters()
                                    + getTrackedRace().getTrackedLeg(leg)
                                            .getAbsoluteWindwardDistance(currentPosition, leaderPosition, timePoint, windPositionMode, cache)
                                            .getMeters());
                            break;
                        }
                    }
                }
            } finally {
                getTrackedRace().getRace().getCourse().unlockAfterRead();
            }
        }
        return result;
    }

    @Override
    public Distance getAverageAbsoluteCrossTrackError(TimePoint timePoint, boolean waitForLatestAnalysis) throws NoWindException {
        final Distance result;
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart != null) {
            final TimePoint to = getTimePointNotAfterFinishingOfLeg(timePoint);
            if (to != null) {
                result = getTrackedRace().getAverageAbsoluteCrossTrackError(competitor, legStart.getTimePoint(), to,
                        /* upwindOnly */ false, waitForLatestAnalysis);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Distance getAverageSignedCrossTrackError(TimePoint timePoint, boolean waitForLatestAnalysis) throws NoWindException {
        final Distance result;
        final MarkPassing legStartMarkPassing = getMarkPassingForLegStart();
        if (legStartMarkPassing != null) {
            TimePoint legStart = legStartMarkPassing.getTimePoint();
            final TimePoint to = getTimePointNotAfterFinishingOfLeg(timePoint);
            result = getTrackedRace().getAverageSignedCrossTrackError(competitor, legStart, to, /* upwindOnly */ false, waitForLatestAnalysis);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Duration getGapToLeader(TimePoint timePoint, final Competitor leaderInLegAtTimePoint, WindPositionMode windPositionMode)
            throws NoWindException {
        return getGapToLeader(timePoint, leaderInLegAtTimePoint, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    @Override
    public Duration getGapToLeader(TimePoint timePoint, final Competitor leaderInLegAtTimePoint,
            WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeader(timePoint, ()->leaderInLegAtTimePoint, windPositionMode, cache);
    }

    @FunctionalInterface
    private static interface LeaderGetter {
        Competitor getLeader();
    }

    @Override
    public Duration getGapToLeader(final TimePoint timePoint, WindPositionMode windPositionMode) {
        return getGapToLeader(timePoint, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }

    @Override
    public Duration getGapToLeader(final TimePoint timePoint, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeader(timePoint, ()->getTrackedLeg().getLeader(hasFinishedLeg(timePoint) ? getFinishTime() : timePoint),
                windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    private Duration getGapToLeader(TimePoint timePoint, LeaderGetter leaderGetter, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        // If a competitor already completed this leg, compute the estimated arrival time at the
        // end of this leg and compare to the first mark passing for the end of this leg; if this leg's competitor also already
        // finished the leg, return the difference between this competitor's leg completion time point and the leader's completion
        // time point; else, calculate the windward distance to the leader and divide by
        // the windward speed
        Speed windwardSpeed = getWindwardSpeed(getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(timePoint), timePoint, windPositionMode, cache);
        // Has our competitor started the leg already? If not, we won't be able to compute a gap
        if (hasStartedLeg(timePoint)) {
            Iterable<MarkPassing> markPassingsInOrder = getTrackedRace().getMarkPassingsInOrder(getLeg().getTo());
            if (markPassingsInOrder != null) {
                MarkPassing firstMarkPassing = null;
                getTrackedRace().lockForRead(markPassingsInOrder);
                try {
                    Iterator<MarkPassing> markPassingsForLegEnd = markPassingsInOrder.iterator();
                    if (markPassingsForLegEnd.hasNext()) {
                        firstMarkPassing = markPassingsForLegEnd.next();
                    }
                } finally {
                    getTrackedRace().unlockAfterRead(markPassingsInOrder);
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
                            return whenLeaderFinishedLeg.until(getMarkPassingForLegEnd().getTimePoint()); 
                        } else {
                            if (windwardSpeed == null) {
                                return null;
                            } else {
                                // leader has finished already; our competitor hasn't
                                Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint, windPositionMode);
                                Duration durationSinceLeaderPassedMarkToTimePoint = whenLeaderFinishedLeg.until(timePoint);
                                return windwardSpeed.getDuration(windwardDistanceToGo).plus(durationSinceLeaderPassedMarkToTimePoint);
                            }
                        }
                    }
                }
                // no-one has finished this leg yet at timePoint
                Competitor leader = leaderGetter.getLeader();
                // Maybe our competitor is the leader. Check:
                if (leader == getCompetitor()) {
                    return Duration.NULL; // the leader's gap to the leader
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
                            Distance windwardDistanceToGo = getTrackedLeg().getAbsoluteWindwardDistance(ourEstimatedPosition,
                                    leaderEstimatedPosition, timePoint, windPositionMode);
                            return windwardSpeed.getDuration(windwardDistanceToGo);
                        }
                    }
                }
            }
        }
        // else our competitor hasn't started the leg yet, so we can't compute a gap since we don't
        // have a speed estimate; leave result == null
        return null;
    }

    @Override
    public Duration getCorrectedGapToLeader(TimePoint timePoint, WindPositionMode windPositionMode)
            throws NoWindException {
        // TODO bug 1018, see comment #17: implement the calculation for corrected windward distance
        return getGapToLeader(timePoint, windPositionMode);
    }

    @Override
    public Duration getCorrectedGapToLeader(TimePoint timePoint, WindPositionMode windPositionMode,
            WindLegTypeAndLegBearingCache cache) throws NoWindException {
        // TODO bug 1018, see comment #17: implement the calculation for corrected windward distance
        return getGapToLeader(timePoint, windPositionMode, cache);
    }

    @Override
    public Duration getCorrectedGapToLeader(TimePoint timePoint, Competitor leaderInLegAtTimePoint,
            WindPositionMode windPositionMode) throws NoWindException {
        // TODO bug 1018, see comment #17: implement the calculation for corrected windward distance
        return getGapToLeader(timePoint, leaderInLegAtTimePoint, windPositionMode);
    }

    @Override
    public Duration getCorrectedGapToLeader(TimePoint timePoint, Competitor leaderInLegAtTimePoint,
            WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) throws NoWindException {
        // TODO bug 1018, see comment #17: implement the calculation for corrected windward distance
        return getGapToLeader(timePoint, leaderInLegAtTimePoint, windPositionMode, cache);
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
    public Speed getVelocityMadeGood(TimePoint at, WindPositionMode windPositionMode) throws NoWindException {
        return getVelocityMadeGood(at, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    @Override
    public Speed getVelocityMadeGood(TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        if (hasStartedLeg(at)) {
            TimePoint timePoint;
            if (hasFinishedLeg(at)) {
                // use the leg finishing time point
                timePoint = getMarkPassingForLegEnd().getTimePoint();
            } else {
                timePoint = at;
            }
            SpeedWithBearing speedOverGround = getSpeedOverGround(timePoint);
            return speedOverGround == null ? null : getWindwardSpeed(speedOverGround, timePoint, windPositionMode, cache);
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
    public Duration getEstimatedTimeToNextMark(TimePoint timePoint, WindPositionMode windPositionMode) {
        return getEstimatedTimeToNextMark(timePoint, windPositionMode, new NoCachingWindLegTypeAndLegBearingCache());
    }

    @Override
    public Duration getEstimatedTimeToNextMark(TimePoint timePoint, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        final Duration result;
        if (hasFinishedLeg(timePoint)) {
            result = Duration.NULL;
        } else {
            if (hasStartedLeg(timePoint)) {
                Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint, windPositionMode);
                Speed vmg = getVelocityMadeGood(timePoint, windPositionMode, cache);
                result = vmg == null ? null : vmg.getDuration(windwardDistanceToGo);
            } else {
                result = null;
            }
        }
        return result;
    }

    @Override
    public Distance getManeuverLoss(TimePoint timePointBeforeManeuver,
            TimePoint maneuverTimePoint, TimePoint timePointAfterManeuver) throws NoWindException {
        assert timePointBeforeManeuver != null;
        assert timePointAfterManeuver != null;
        Distance result;
        final GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
        List<GPSFixMoving> fixes = getFixesToConsiderForManeuverLossAnalysis(timePointBeforeManeuver,
                maneuverTimePoint, timePointAfterManeuver);
        TimePoint timePointWhenSpeedStartedToDrop = fixes.get(0).getTimePoint();
        SpeedWithBearing speedWhenSpeedStartedToDrop = track.getEstimatedSpeed(timePointWhenSpeedStartedToDrop);
        if (speedWhenSpeedStartedToDrop != null) {
            TimePoint timePointWhenSpeedLevelledOffAfterManeuver = fixes.get(fixes.size()-1).getTimePoint();
            SpeedWithBearing speedAfterManeuver = track.getEstimatedSpeed(timePointWhenSpeedLevelledOffAfterManeuver);
            if (speedAfterManeuver != null) {
                // For upwind/downwind legs, find the mean course between inbound and outbound course and project actual and
                // extrapolated positions onto it:
                Bearing middleManeuverAngle = speedWhenSpeedStartedToDrop.getBearing().middle(speedAfterManeuver.getBearing());
                // extrapolate maximum speed before maneuver to time point of maximum speed after maneuver and project resulting position
                // onto the average maneuver course; compare to the projected position actually reached at the time point of maximum speed after
                // maneuver:
                Position positionWhenSpeedStartedToDrop = track.getEstimatedPosition(timePointWhenSpeedStartedToDrop, /* extrapolate */ false);
                Position extrapolatedPositionAtTimePointOfMaxSpeedAfterManeuver = 
                        speedWhenSpeedStartedToDrop.travelTo(positionWhenSpeedStartedToDrop, timePointWhenSpeedStartedToDrop, timePointWhenSpeedLevelledOffAfterManeuver);
                Position actualPositionAtTimePointOfMaxSpeedAfterManeuver = track.getEstimatedPosition(timePointWhenSpeedLevelledOffAfterManeuver, /* extrapolate */ false);
                Position projectedExtrapolatedPositionAtTimePointOfMaxSpeedAfterManeuver =
                        extrapolatedPositionAtTimePointOfMaxSpeedAfterManeuver.projectToLineThrough(positionWhenSpeedStartedToDrop, middleManeuverAngle);
                Position projectedActualPositionAtTimePointOfMaxSpeedAfterManeuver =
                        actualPositionAtTimePointOfMaxSpeedAfterManeuver.projectToLineThrough(positionWhenSpeedStartedToDrop, middleManeuverAngle);
                result = projectedActualPositionAtTimePointOfMaxSpeedAfterManeuver.getDistance(projectedExtrapolatedPositionAtTimePointOfMaxSpeedAfterManeuver);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Fetches the set of fixes that encompass the maneuver. Usually, during a maneuver a boat loses speed over ground
     * before it has reached the new tack and starts accelerating again, until the speed levels off. We assume that the
     * <code>timePointBeforeManeuver</code> minus some excess time based on the approximate maneuver duration is the
     * earliest time point to analyze. Starting there, we look for speed over ground minima between then and
     * <code>timePointAfterManeuver</code> plus three times the approximate maneuver time as excess time. There may be
     * multiple minima. We choose the one that has the best "fit" in terms of being close to the
     * <code>maneuverTimePoint</code> and being low in terms of speed over ground. From that time point, the nearest
     * maximum speeds over ground before and after are determined, and the fixes between them are returned.
     */
    private List<GPSFixMoving> getFixesToConsiderForManeuverLossAnalysis(TimePoint timePointBeforeManeuver,
            TimePoint maneuverTimePoint, TimePoint timePointAfterManeuver) {
        final long EXCESS_TIME_BEFORE_MANEUVER_END_TO_SCAN_IN_MILLIS = getCompetitor().getBoat().getBoatClass().getApproximateManeuverDurationInMilliseconds();
        final long EXCESS_TIME_AFTER_MANEUVER_END_TO_SCAN_IN_MILLIS = 3*EXCESS_TIME_BEFORE_MANEUVER_END_TO_SCAN_IN_MILLIS;
        List<GPSFixMoving> fixes = new ArrayList<>();
        NavigableSet<GPSFixMoving> maxima = new ArrayListNavigableSet<GPSFixMoving>(new TimedComparator());
        NavigableSet<GPSFixMoving> minima = new ArrayListNavigableSet<GPSFixMoving>(new TimedComparator());
        GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
        Speed lastSpeed = Speed.NULL;
        Speed lastLastSpeed = Speed.NULL;
        GPSFixMoving lastFix = null;
        Speed minimumSpeed = new KnotSpeedImpl(Double.MAX_VALUE);
        track.lockForRead();
        try {
            Iterator<GPSFixMoving> fixIter = track.getFixesIterator(
                    timePointBeforeManeuver.minus(EXCESS_TIME_BEFORE_MANEUVER_END_TO_SCAN_IN_MILLIS), /* inclusive */true);
            GPSFixMoving fix;
            // The timePointAfterManeuver is determined based on the geometric shape of the boat's trajectory, not on
            // the speed development. To understand the full maneuver loss, we need to follow the boat speed until it levels off,
            // but no further than some reasonable threshold because the wind may continue to pick up, letting the boat accelerate
            // over a time much longer than accounted for by the maneuver.
            while (fixIter.hasNext()) {
                fix = fixIter.next();
                final SpeedWithBearing estimatedSpeedAtFix = track.getEstimatedSpeed(fix.getTimePoint());
                if (lastFix != null && lastSpeed != null && lastLastSpeed != null && estimatedSpeedAtFix != null) {
                    if (lastSpeed.compareTo(lastLastSpeed) > 0 && lastSpeed.compareTo(estimatedSpeedAtFix) > 0) {
                        maxima.add(lastFix);
                    } else if (lastSpeed.compareTo(lastLastSpeed) < 0 && lastSpeed.compareTo(estimatedSpeedAtFix) < 0) {
                        minima.add(lastFix);
                        if (lastSpeed.compareTo(minimumSpeed) < 0) {
                            minimumSpeed = lastSpeed;
                        }
                    }
                }
                if (fix.getTimePoint().after(timePointAfterManeuver.plus(EXCESS_TIME_AFTER_MANEUVER_END_TO_SCAN_IN_MILLIS))) {
                    break;
                }
                fixes.add(fix);
                lastLastSpeed = lastSpeed;
                lastSpeed = estimatedSpeedAtFix;
                lastFix = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
        GPSFixMoving fixWithLowestSpeedOverGround = getBestFittingSpeedMinimumInManeuver(minima, maxima, minimumSpeed, maneuverTimePoint);
        if (fixWithLowestSpeedOverGround == null) {
            fixWithLowestSpeedOverGround = fixes.get(0);
        }
        // now remove all fixes before the last maximum before the fix with the lowest speed over ground during the maneuver if
        // there was such a maximum; otherwise, leave all fixes from the maneuver start on in place.
        final long MAX_SMOOTHENING_INTERVAL_MILLIS = getCompetitor().getBoat().getBoatClass().getApproximateManeuverDurationInMilliseconds();
        GPSFixMoving lastMaxSpeedFixBeforeLowSpeed = maxima.lower(fixWithLowestSpeedOverGround);
        // now check if there's a greater one that's only a little bit earlier
        if (lastMaxSpeedFixBeforeLowSpeed != null) {
            GPSFixMoving stillGreater;
            while ((stillGreater=maxima.lower(lastMaxSpeedFixBeforeLowSpeed)) != null &&
                    track.getEstimatedSpeed(stillGreater.getTimePoint()).compareTo(
                            track.getEstimatedSpeed(lastMaxSpeedFixBeforeLowSpeed.getTimePoint())) > 0 &&
                    lastMaxSpeedFixBeforeLowSpeed.getTimePoint().asMillis()-
                    stillGreater.getTimePoint().asMillis() < MAX_SMOOTHENING_INTERVAL_MILLIS) {
                lastMaxSpeedFixBeforeLowSpeed = stillGreater;
            }
            Iterator<GPSFixMoving> i = fixes.iterator();
            while (i.hasNext() && i.next().getTimePoint().before(lastMaxSpeedFixBeforeLowSpeed.getTimePoint())) {
                i.remove();
            }
        }
        // now remove all fixes after the first maximum after the global minimum:
        GPSFixMoving firstMaxSpeedFixAfterLowSpeed = maxima.higher(fixWithLowestSpeedOverGround);
        if (firstMaxSpeedFixAfterLowSpeed != null) {
            GPSFixMoving stillGreater;
            while ((stillGreater=maxima.higher(firstMaxSpeedFixAfterLowSpeed)) != null &&
                    track.getEstimatedSpeed(stillGreater.getTimePoint()).compareTo(
                            track.getEstimatedSpeed(firstMaxSpeedFixAfterLowSpeed.getTimePoint())) > 0 &&
                    stillGreater.getTimePoint().asMillis() - firstMaxSpeedFixAfterLowSpeed.getTimePoint().asMillis()
                     < MAX_SMOOTHENING_INTERVAL_MILLIS) {
                firstMaxSpeedFixAfterLowSpeed = stillGreater;
            }
            ListIterator<GPSFixMoving> i = fixes.listIterator(fixes.size());
            while (i.hasPrevious() && i.previous().getTimePoint().after(firstMaxSpeedFixAfterLowSpeed.getTimePoint())) {
                i.remove();
            }
        }
        return fixes;
    }

    private GPSFixMoving getBestFittingSpeedMinimumInManeuver(NavigableSet<GPSFixMoving> minima,
            NavigableSet<GPSFixMoving> maxima, Speed minimumSpeed, TimePoint maneuverTimePoint) {
        // Idea: being 10x the approximate maneuver duration away from maneuverTimePoint is as bad as having twice the percentage between min
        // and max speed (min = 0%; max=100%).
        GPSFixMoving bestSpeedMinimum = null;
        Speed maxSpeed = null;
        for (GPSFixMoving maximum : maxima) {
            if (maxSpeed == null || maximum.getSpeed().getKnots() > maxSpeed.getKnots()) {
                maxSpeed = maximum.getSpeed();
            }
        }
        if (maxSpeed != null) {
            double speedDifferenceBetweenMaxAndMinInKnots = maxSpeed.getKnots() - minimumSpeed.getKnots();
            double lowestBadness = Double.MAX_VALUE;
            final long approximateManeuverTimeInMillis = getCompetitor().getBoat().getBoatClass()
                    .getApproximateManeuverDurationInMilliseconds();
            final GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
            for (GPSFixMoving minimum : minima) {
                // best speedBadness can be 1 which represents the absolute speed minimum in the interval considered
                double speedBadness = 1 + (track.getEstimatedSpeed(minimum.getTimePoint()).getKnots() - minimumSpeed.getKnots()) /
                        speedDifferenceBetweenMaxAndMinInKnots;
                // best timePointBadness can be 1 which is a minimum exactly at the maneuver time point
                double timePointBadness = 1.
                        + (double) Math.abs(minimum.getTimePoint().asMillis() - maneuverTimePoint.asMillis())
                        / (double) approximateManeuverTimeInMillis / 10.;
                final double totalBadness = speedBadness * timePointBadness;
                if (totalBadness < lowestBadness) {
                    bestSpeedMinimum = minimum;
                    lowestBadness = totalBadness;
                }
            }
        }
        return bestSpeedMinimum;
    }
    
    @Override
    public Bearing getBeatAngle(TimePoint at) throws NoWindException {
        return getBeatAngle(at, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    @Override
    public Bearing getBeatAngle(TimePoint at, WindLegTypeAndLegBearingCache cache) throws NoWindException {
        Bearing beatAngle = null;
        Bearing projectToBearing;
        Wind wind = cache.getWind(getTrackedRace(), getCompetitor(), at);
        if (wind == null) {
            throw new NoWindException("Need at least wind direction to determine windward speed");
        }
        projectToBearing = wind.getFrom();
        SpeedWithBearing speed = getSpeedOverGround(at);
        if (speed != null) {
            beatAngle = speed.getBearing().getDifferenceTo(projectToBearing);
        }
        return beatAngle;
    }
}
