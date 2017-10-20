package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
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
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

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
            result = timePointNotAfterFinishingOfLeg == null ? null : passedStartWaypoint.getTimePoint().until(timePointNotAfterFinishingOfLeg);
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
                result = d.inTime(legStart.getTimePoint().until(timePointToUse));
            } else {
                result = null;
            }
        }
        return result;
    }

    @Override
    public Distance getAverageRideHeight(TimePoint timePoint) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart != null) {
            BravoFixTrack<Competitor> track = getTrackedRace()
                    .<BravoFix, BravoFixTrack<Competitor>> getSensorTrack(getCompetitor(), BravoFixTrack.TRACK_NAME);
            if (track != null) {
                TimePoint endTimePoint = hasFinishedLeg(timePoint) ? getMarkPassingForLegEnd().getTimePoint() : timePoint;
                return track.getAverageRideHeight(legStart.getTimePoint(), endTimePoint);
            }
        }
        return null;
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
        return getWindwardDistanceToGo(timePoint, windPositionMode, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    /**
     * If the current {@link #getLeg() leg} is +/- {@link LegType#UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees collinear with the
     * wind's bearing, the competitor's position is projected onto the line crossing <code>mark</code> in the wind's
     * bearing, and the distance from the projection to the <code>mark</code> is returned. Otherwise, it is assumed that
     * the leg is neither an upwind nor a downwind leg, and hence the true distance to <code>mark</code> is returned. A
     * cache for wind and leg type / bearing can be passed to avoid their redundant calculation during a single
     * round-trip.
     * <p>
     * 
     * If no wind information is available, again the true geometrical distance to <code>mark</code> is returned.
     * <p>
     * 
     * If the competitor's position or the mark's position cannot be determined, <code>null</code> is returned.
     * <code>null</code> is also returned if the leg's bearing cannot be determined because for at least one of its two
     * waypoints no mark has a known position.
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
            if (speed.getBearing() != null && projectToBearing != null) {
                double cos = Math.cos(speed.getBearing().getRadians() - projectToBearing.getRadians());
                if (cos < 0) {
                    projectToBearing = projectToBearing.reverse();
                }
                result = new KnotSpeedWithBearingImpl(Math.abs(speed.getKnots() * cos), projectToBearing);
            }
        }
        return result;
    }

    /**
     * Calculates the competitor's rank at {@code timePoint} based on the {@link WindPositionMode#LEG_MIDDLE} wind
     * direction for upwind and downwind legs, or based on the leg's rhumb line for reaching legs.
     */
    @Override
    public int getRank(TimePoint timePoint) {
        return getRank(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
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
        return getAverageVelocityMadeGood(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
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
                final Position startPos = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(start.getTimePoint(), false);
                if (startPos != null) {
                    Distance d = getTrackedLeg().getAbsoluteWindwardDistance(startPos, endPos, to,
                            WindPositionMode.EXACT, cache);
                    result = d == null ? null : d.inTime(to.asMillis() - start.getTimePoint().asMillis());
                }
            }
        }
        return result;
    }

    
    @Override
    public Integer getNumberOfTacks(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            Iterable<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
    public Iterable<Maneuver> getManeuvers(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        final Iterable<Maneuver> maneuvers;
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            maneuvers = Collections.emptyList();
        } else {
            TimePoint start = legStart.getTimePoint();
            MarkPassing legEnd = getMarkPassingForLegEnd();
            TimePoint end = timePoint;
            if (legEnd != null && timePoint.compareTo(legEnd.getTimePoint()) > 0) {
                // timePoint is after leg finish; take leg end and end time point
                end = legEnd.getTimePoint();
            }
            maneuvers = getTrackedRace().getManeuvers(getCompetitor(),
                    start, end, waitForLatest);
        }
        return maneuvers;
    }

    @Override
    public Integer getNumberOfJibes(TimePoint timePoint, boolean waitForLatest) throws NoWindException {
        Integer result = null;
        if (hasStartedLeg(timePoint)) {
            Iterable<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
            Iterable<Maneuver> maneuvers = getManeuvers(timePoint, waitForLatest);
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
    public Distance getWindwardDistanceToCompetitorFarthestAhead(TimePoint timePoint, WindPositionMode windPositionMode, final RankingInfo rankingInfo) {
        return getWindwardDistanceToCompetitorFarthestAhead(timePoint, windPositionMode, rankingInfo, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    @Override
    public Distance getWindwardDistanceToCompetitorFarthestAhead(TimePoint timePoint, WindPositionMode windPositionMode, final RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        // FIXME bug 607 it seems the following fetches the leader of this leg, not the overall leader; validate!!! Use getTrackedRace().getRanks() instead
        Competitor competitorFarthestAhead = rankingInfo.getCompetitorFarthestAhead();
        TrackedLegOfCompetitor leaderLeg = getTrackedRace().getCurrentLeg(competitorFarthestAhead, timePoint);
        Distance result = null;
        Position leaderPosition = getTrackedRace().getTrack(competitorFarthestAhead).getEstimatedPosition(timePoint, /* extrapolate */ false);
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
                            if (nextMarkPosition == null) {
                                result = null;
                                break;
                            } else {
                                Distance distanceToNextMark = getTrackedRace().getTrackedLeg(leg)
                                        .getAbsoluteWindwardDistance(currentPosition, nextMarkPosition, timePoint, windPositionMode, cache);
                                if (distanceToNextMark != null) {
                                    result = new MeterDistance(result.getMeters() + distanceToNextMark.getMeters());
                                } else {
                                    result = null;
                                    break;
                                }
                            }
                            currentPosition = nextMarkPosition;
                        } else {
                            // we're now in the same leg with leader; compute windward distance to leader
                            final Distance absoluteWindwardDistance = getTrackedRace().getTrackedLeg(leg)
                                    .getAbsoluteWindwardDistance(currentPosition, leaderPosition, timePoint, windPositionMode, cache);
                            if (absoluteWindwardDistance != null) {
                                result = new MeterDistance(result.getMeters() + absoluteWindwardDistance.getMeters());
                            } else {
                                result = null;
                            }
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
    public Duration getGapToLeader(TimePoint timePoint, final Competitor leaderInLegAtTimePoint,
            final RankingInfo rankingInfo, WindPositionMode windPositionMode) throws NoWindException {
        return getGapToLeader(timePoint, leaderInLegAtTimePoint, windPositionMode, rankingInfo, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    @Override
    public Duration getGapToLeader(TimePoint timePoint, final Competitor leaderInLegAtTimePoint,
            WindPositionMode windPositionMode, final RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeader(timePoint, ()->leaderInLegAtTimePoint, windPositionMode, rankingInfo, cache);
    }

    @FunctionalInterface
    private static interface LeaderGetter {
        Competitor getLeader();
    }

    @Override
    public Duration getGapToLeader(final TimePoint timePoint, final RankingInfo rankingInfo, WindPositionMode windPositionMode) {
        return getGapToLeader(timePoint, windPositionMode, rankingInfo, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    @Override
    public Duration getGapToLeader(final TimePoint timePoint, WindPositionMode windPositionMode, final RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeader(timePoint, ()->getTrackedLeg().getLeader(hasFinishedLeg(timePoint) ? getFinishTime() : timePoint),
                windPositionMode, rankingInfo, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    private Duration getGapToLeader(TimePoint timePoint, LeaderGetter leaderGetter, WindPositionMode windPositionMode, RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache) {
        // If a competitor already completed this leg, compute the estimated arrival time at the
        // end of this leg and compare to the first mark passing for the end of this leg; if this leg's competitor also already
        // finished the leg, return the difference between this competitor's leg completion time point and the leader's completion
        // time point; else, calculate the windward distance to the leader and divide by
        // the windward speed
        // See also bug1080: using the average VMG instead of the current VMG may produce better results
        Speed windwardSpeed = getAverageVelocityMadeGood(timePoint, cache);
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
    public Speed getVelocityMadeGood(TimePoint at, WindPositionMode windPositionMode) {
        return getVelocityMadeGood(at, windPositionMode, new LeaderboardDTOCalculationReuseCache(at));
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
    public Bearing getHeel(TimePoint at) {
        final Bearing result;
        if (hasStartedLeg(at)) {
            TimePoint timePoint = hasFinishedLeg(at) ? getMarkPassingForLegEnd().getTimePoint() : at;
            BravoFixTrack<Competitor> track = getTrackedRace()
                    .<BravoFix, BravoFixTrack<Competitor>> getSensorTrack(competitor, BravoFixTrack.TRACK_NAME);
            result = track == null ? null : track.getHeel(timePoint);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Bearing getPitch(TimePoint at) {
        final Bearing result;
        if (hasStartedLeg(at)) {
            TimePoint timePoint = hasFinishedLeg(at) ? getMarkPassingForLegEnd().getTimePoint() : at;
            BravoFixTrack<Competitor> track = getTrackedRace()
                    .<BravoFix, BravoFixTrack<Competitor>> getSensorTrack(competitor, BravoFixTrack.TRACK_NAME);
            result = track == null ? null : track.getPitch(timePoint);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Distance getRideHeight(TimePoint at) {
        final Distance result;
        if (hasStartedLeg(at)) {
            TimePoint timePoint =hasFinishedLeg(at) ? getMarkPassingForLegEnd().getTimePoint() : at;
            BravoFixTrack<Competitor> track = getTrackedRace()
                    .<BravoFix, BravoFixTrack<Competitor>> getSensorTrack(competitor, BravoFixTrack.TRACK_NAME);
            result = track == null ? null : track.getRideHeight(timePoint);
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public Duration getEstimatedTimeToNextMark(TimePoint timePoint, WindPositionMode windPositionMode) {
        return getEstimatedTimeToNextMark(timePoint, windPositionMode, new LeaderboardDTOCalculationReuseCache(timePoint));
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
        Pair<TimePoint, TimePoint> startAndEndOfManeuver = getStartAndEndOfManeuverToConsiderForManeuverLossAnalysis(timePointBeforeManeuver,
                maneuverTimePoint, timePointAfterManeuver);
        if (startAndEndOfManeuver.getA() == null || startAndEndOfManeuver.getB() == null) {
            result = null;
        } else {
            TimePoint timePointWhenSpeedStartedToDrop = startAndEndOfManeuver.getA();
            SpeedWithBearing speedWhenSpeedStartedToDrop = track.getEstimatedSpeed(timePointWhenSpeedStartedToDrop);
            if (speedWhenSpeedStartedToDrop != null) {
                TimePoint timePointWhenSpeedLevelledOffAfterManeuver = startAndEndOfManeuver.getB();
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
        }
        return result;
    }

    /**
     * Fetches the start and end time point of the maneuver such that the speed and course values ideally represent
     * stable segments leading into and out of the maneuver, therefore eligible for maneuver loss analysis. The start
     * time point / course is identified as the first maximum SOG at or after the {@code timePointBeforeManeuver}; this
     * means that if the SOG is decreasing at that time point then {@code timePointBeforeManeuver} is used as that
     * maneuver start time.
     * <p>
     * 
     * For the end of the maneuver things are a bit more tricky as the SOG takes a while to raise to a stable level
     * again even after the competitor has reached the new target course after the maneuver. The first SOG maximum after
     * the end of the maneuver could be a local maximum that is still somewhere in the acceleration phase. Because of
     * this, at least one {@link BoatClass#getApproximateManeuverDurationInMilliseconds() maneuver duration} is
     * traversed after the end of the maneuver to search for a speed maximum. If the speed is still increasing at the
     * end of this interval, the search for a maximum continues up to three times the
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() maneuver duration} after the end of the maneuver.
     * <p>
     */
    private Pair<TimePoint, TimePoint> getStartAndEndOfManeuverToConsiderForManeuverLossAnalysis(TimePoint timePointBeforeManeuver,
            TimePoint maneuverTimePoint, TimePoint timePointAfterManeuver) {
        final long EXCESS_TIME_AFTER_MANEUVER_END_TO_SCAN_IN_MILLIS = 3*getCompetitor().getBoat().getBoatClass().getApproximateManeuverDurationInMilliseconds();
        GPSFixMoving maxSpeedFixBeforeManeuver = findFirstMaxSOG(timePointBeforeManeuver,
                /* scanAtLeast */ Duration.NULL, /* latestTimePoint */ timePointAfterManeuver.plus(EXCESS_TIME_AFTER_MANEUVER_END_TO_SCAN_IN_MILLIS));
        GPSFixMoving maxSpeedFixAfterManeuver = findFirstMaxSOG(timePointAfterManeuver,
                /* scanAtLeast */ getCompetitor().getBoat().getBoatClass().getApproximateManeuverDuration(),
                /* latestTimePoint */ timePointAfterManeuver.plus(EXCESS_TIME_AFTER_MANEUVER_END_TO_SCAN_IN_MILLIS));
        return new Pair<>(maxSpeedFixBeforeManeuver==null?null:maxSpeedFixBeforeManeuver.getTimePoint(),
                maxSpeedFixAfterManeuver==null?null:maxSpeedFixAfterManeuver.getTimePoint());
    }

    /**
     * Search for a SOG maximum in the {@link #getCompetitor() competitor's} track, starting at {@code findMaxSpeedStartingAt}.
     * In any case, a duration of {@code scanAtLeast} is scanned for such a maximum. After that the search ends with the
     * first SOG decrease. This means that a maximum will be found between {@code findMaxSpeedStartingAt} and
     * {@code findMaxSpeedStartingAt.plus(scanAtLeast)} except if at the end of this interval the speed is increasing
     * further. In this case, the first speed maximum after this interval and no later than {@code latestTimepoint} is returned.
     * 
     * @param findMaxSpeedStartingAt
     *            start here in the {@link #getCompetitor() competitor's} track
     */
    private GPSFixMoving findFirstMaxSOG(final TimePoint findMaxSpeedStartingAt,
            Duration scanAtLeast, TimePoint latestTimePoint) {
        GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
        GPSFixMoving result = null;
        SpeedWithBearing maxSpeed = null;
        GPSFixMoving previousFix = null;
        SpeedWithBearing previousSpeed = null;
        track.lockForRead();
        try {
            Iterator<GPSFixMoving> fixIter = track.getFixesIterator(findMaxSpeedStartingAt, /* inclusive */true);
            GPSFixMoving fix;
            // The timePointAfterManeuver is determined based on the geometric shape of the boat's trajectory, not on
            // the speed development. To understand the full maneuver loss, we need to follow the boat speed until it levels off,
            // but no further than some reasonable threshold because the wind may continue to pick up, letting the boat accelerate
            // over a time much longer than accounted for by the maneuver.
            while (fixIter.hasNext()) {
                fix = fixIter.next();
                if (fix.getTimePoint().after(latestTimePoint)) { // reached ultimate end of scan
                    break;
                }
                final SpeedWithBearing estimatedSpeedAtFix = track.getEstimatedSpeed(fix.getTimePoint());
                if (estimatedSpeedAtFix != null && (maxSpeed == null || estimatedSpeedAtFix.compareTo(maxSpeed) > 0)) {
                    // a new speed maximum; record it
                    maxSpeed = estimatedSpeedAtFix;
                    result = fix;
                }
                if (previousFix != null && previousSpeed != null && estimatedSpeedAtFix != null && previousSpeed.compareTo(estimatedSpeedAtFix) > 0) {
                    // speed decreased compared to previous fix; stop scanning if after findMaxSpeedStartingAt.plus(scanAtLeast):
                    if (fix.getTimePoint().after(findMaxSpeedStartingAt.plus(scanAtLeast))) {
                        break;
                    }
                }
                previousSpeed = estimatedSpeedAtFix;
                previousFix = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
        return result;
    }

    @Override
    public Bearing getBeatAngle(TimePoint at) throws NoWindException {
        return getBeatAngle(at, new LeaderboardDTOCalculationReuseCache(at));
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
    

    @Override
    public String toString() {
        return "TrackedLegOfCompetitor for "+getCompetitor()+" in leg "+getLeg();
    }
}
