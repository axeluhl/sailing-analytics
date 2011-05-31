package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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
    private final double UPWIND_DOWNWIND_TOLERANCE_IN_DEG = 40; // TracTrac does 22.5, Marcus Baur suggest 40
    
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
    public long getTimeInMilliSeconds() {
        long result = -1;
        MarkPassing passedEndWaypoint = getTrackedRace().getMarkPassing(getCompetitor(), getTrackedLeg().getLeg().getTo());
        if (passedEndWaypoint != null) {
            MarkPassing passedStartWaypoint = getTrackedRace().getMarkPassing(getCompetitor(), getTrackedLeg().getLeg().getFrom());
            if (passedStartWaypoint != null) {
                result = passedEndWaypoint.getTimePoint().asMillis() - passedStartWaypoint.getTimePoint().asMillis();
            } else {
                result = 0;
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
            MarkPassing legEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
            TimePoint end;
            if (legEnd == null) {
                // leg not yet finished; take time specified
                end = timePoint;
            } else {
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
            TimePoint now = MillisecondsTimePoint.now();
            Distance d = getDistanceTraveled(now);
            long millis = getTimeInMilliSeconds();
            if (millis == -1) {
                // didn't finish the leg yet
                millis = now.asMillis() - legStart.getTimePoint().asMillis();
            }
            return d.inTime(millis);
        }
    }

    @Override
    public Speed getMaximumSpeedOverGround() {
        // fetch all fixes on this leg so far and determine their maximum speed
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return null;
        }
        GPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(getCompetitor());
        Iterator<GPSFixMoving> iter = track.getFixesIterator(legStart.getTimePoint(), /* inclusive */ false);
        Speed max = Speed.NULL;
        if (iter.hasNext()) {
            TimePoint markPassingTimeMillis = getMarkPassingForLegStart().getTimePoint();
            Position lastPos = track.getEstimatedPosition(markPassingTimeMillis);
            long lastTimeMillis = markPassingTimeMillis.asMillis();
            while (iter.hasNext()) {
                GPSFixMoving fix = iter.next();
                Speed fixSpeed = fix.getSpeed();
                Speed calculatedSpeed = lastPos.getDistance(fix.getPosition()).inTime(fix.getTimePoint().asMillis()-lastTimeMillis);
                Speed averaged = averageSpeed(fixSpeed, calculatedSpeed);
                if (averaged.compareTo(max) > 0) {
                    max = averaged;
                }
            }
        }
        return max;
    }

    private Speed averageSpeed(Speed... speeds) {
        double sumInKMH = 0;
        int count = 0;
        for (Speed speed : speeds) {
            sumInKMH += speed.getKilometersPerHour();
            count++;
        }
        return new KilometersPerHourSpeedImpl(sumInKMH/count);
    }

    @Override
    public Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException {
        if (hasFinishedLeg(timePoint)) {
            return Distance.NULL; // 
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
        return getWindwardDistance(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at),
                getTrackedRace().getTrack(buoy).getEstimatedPosition(at), at);
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
        if (isUpOrDownwindLeg(at)) {
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
        Wind wind = getWind(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at), at);
        if (wind == null) {
            throw new NoWindException("Need at least wind direction to determine windward speed");
        }
        Bearing bearing = wind.getBearing();
        double cos = Math.cos(speed.getBearing().getRadians()-wind.getBearing().getRadians());
        if (cos < 0) {
            bearing = bearing.reverse();
        }
        SpeedWithBearing result = new KnotSpeedImpl(Math.abs(wind.getKnots() * cos), bearing);
        return result;
    }

    /**
     * For now, we have an incredibly simple wind "model" which assigns a single common wind force and bearing
     * to all positions on the course, only variable over time.
     */
    private Wind getWind(Position p, TimePoint at) {
        return getTrackedRace().getWind(p, at);
    }

    /**
     * Determines whether the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the current wind's bearing.
     */
    private boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException {
        Wind wind = getWindOnLeg(at);
        if (wind == null) {
            throw new NoWindException("Need to know wind direction to determine whether leg "+getLeg()+
                    " is an upwind or downwind leg");
        }
        // check for all combinations of start/end waypoint buoys:
        for (Buoy startBuoy : getLeg().getFrom().getBuoys()) {
            Position startBuoyPos = getTrackedRace().getTrack(startBuoy).getEstimatedPosition(at);
            for (Buoy endBuoy : getLeg().getTo().getBuoys()) {
                Position endBuoyPos = getTrackedRace().getTrack(endBuoy).getEstimatedPosition(at);
                Bearing legBearing = startBuoyPos.getBearingGreatCircle(endBuoyPos);
                double deltaDeg = legBearing.getDegrees() - wind.getBearing().getDegrees();
                if (deltaDeg > 180) {
                    deltaDeg -= 180;
                } else if (deltaDeg < 0) {
                    deltaDeg += 180;
                }
                if (deltaDeg < UPWIND_DOWNWIND_TOLERANCE_IN_DEG) {
                    return true;
                }
            }
        }
        return false;
    }

    private Wind getWindOnLeg(TimePoint at) {
        Position approximateLegStartPosition = getTrackedRace().getTrack(
                getLeg().getFrom().getBuoys().iterator().next()).getEstimatedPosition(at);
        Position approximateLegEndPosition = getTrackedRace().getTrack(
                getLeg().getTo().getBuoys().iterator().next()).getEstimatedPosition(at);
        Wind wind = getWind(
                approximateLegStartPosition.translateGreatCircle(approximateLegStartPosition.getBearingGreatCircle(approximateLegEndPosition),
                        approximateLegStartPosition.getDistance(approximateLegEndPosition)), at);
        return wind;
    }

    @Override
    public int getRank(TimePoint timePoint) {
        TreeSet<TrackedLegOfCompetitor> competitorTracksByRank = getTrackedLeg().getCompetitorTracksOrderedByRank(timePoint);
        return competitorTracksByRank.headSet(this).size()+1;
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        Speed result = null;
        MarkPassing start = getMarkPassingForLegStart();
        if (start != null && start.getTimePoint().compareTo(timePoint) > 0) {
            MarkPassing end = getMarkPassingForLegEnd();
            TimePoint to;
            if (timePoint.compareTo(end.getTimePoint()) >= 0) {
                to = end.getTimePoint();
            } else {
                to = timePoint;
            }
            Position endPos = getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(to);
            Distance d = getWindwardDistance(
                    getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(start.getTimePoint()), endPos, to);
            result = d.inTime(to.asMillis()-start.getTimePoint().asMillis());
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
    public double getGapToLeaderInSeconds(TimePoint timePoint) throws NoWindException {
        // If the leader already completed this leg, compute the estimated arrival time at the
        // end of this leg; if this leg's competitor also already finished the leg, return the
        // difference between this competitor's leg completion time and the leader's completion
        // time; else, calculate the windward distance to the leader and divide by
        // the windward speed
        Speed windwardSpeed = getWindwardSpeed(getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(timePoint), timePoint);
        Iterator<MarkPassing> markPassingsForLegEnd = getTrackedRace().getMarkPassingsInOrder(getLeg().getTo()).iterator();
        if (markPassingsForLegEnd.hasNext()) {
            TimePoint whenLeaderFinishedLeg = markPassingsForLegEnd.next().getTimePoint();
            if (whenLeaderFinishedLeg.compareTo(timePoint) <= 0) {
                if (hasFinishedLeg(timePoint)) {
                    return (getMarkPassingForLegEnd().getTimePoint().asMillis()-whenLeaderFinishedLeg.asMillis())/1000;
                } else {
                    Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint);
                    long millisSinceLeaderPassedMarkToTimePoint = timePoint.asMillis()-whenLeaderFinishedLeg.asMillis();
                    return windwardDistanceToGo.getMeters() / windwardSpeed.getMetersPerSecond() + millisSinceLeaderPassedMarkToTimePoint/1000;
                }
            }
        }
        Competitor leader = getTrackedLeg().getLeader(timePoint);
        Distance windwardDistanceToGo = getWindwardDistance(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(timePoint),
                getTrackedRace().getTrack(leader).getEstimatedPosition(timePoint), timePoint);
        return windwardDistanceToGo.getMeters() / windwardSpeed.getMetersPerSecond();
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
        return getWindwardSpeed(getSpeedOverGround(at), at);
    }

    private SpeedWithBearing getSpeedOverGround(TimePoint at) {
        return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(at);
    }

    @Override
    public double getEstimatedTimeToNextMarkInSeconds(TimePoint timePoint) throws NoWindException {
        double result;
        if (hasFinishedLeg(timePoint)) {
            result = 0;
        } else {
            Distance windwardDistanceToGo = getWindwardDistanceToGo(timePoint);
            Speed vmg = getVelocityMadeGood(timePoint);
            result = windwardDistanceToGo.getMeters() / vmg.getMetersPerSecond();
        }
        return result;
    }

}
