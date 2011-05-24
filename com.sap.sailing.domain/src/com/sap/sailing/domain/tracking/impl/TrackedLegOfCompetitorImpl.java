package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
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
            result = passedEndWaypoint.getTimePoint().asMillis() - getTrackedRace().getStart().asMillis();
        }
        return result;
    }

    @Override
    public Distance getDistanceTraveled() {
        return getDistanceTraveled(MillisecondsTimePoint.now());
    }
    
    private Distance getDistanceTraveled(TimePoint until) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return Distance.NULL;
        } else {
            MarkPassing legEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
            TimePoint end;
            if (legEnd == null) {
                // leg not yet finished; take time specified
                end = until;
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
    public Speed getAverageSpeedOverGround() {
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
        Iterator<GPSFixMoving> iter = track.getFixes(legStart.getTimePoint(), /* inclusive */ false);
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
    public Distance getWindwardDistanceToGo() {
        if (getMarkPassingForLegEnd() != null) {
            return Distance.NULL;
        } else {
            Distance result = null;
            for (Buoy buoy : getLeg().getTo().getBuoys()) {
                Distance d = getWindwardDistanceTo(buoy);
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
    private Distance getWindwardDistanceTo(Buoy buoy) {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        return getWindwardDistanceTo(buoy, now);
    }

    private Distance getWindwardDistanceTo(Buoy buoy, TimePoint at) {
        return getWindwardDistance(getTrackedRace().getTrack(getCompetitor()).getEstimatedPosition(at),
                getTrackedRace().getTrack(buoy).getEstimatedPosition(at), at);
    }

    /**
     * If the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees collinear with the
     * wind's bearing, the competitor's position is projected onto the line crossing <code>buoy</code> in the wind's
     * bearing, and the distance from the projection to the <code>buoy</code> is returned. Otherwise, it is assumed that
     * the leg is neither an upwind nor a downwind leg, and hence the true distance to <code>buoy</code> is returned.
     */
    private Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at) {
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
    private boolean isUpOrDownwindLeg(TimePoint at) {
        Wind wind = getWindOnLeg(at);
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
    public int getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Speed getAverageVelocityMadeGood() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfTacks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfJibes() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfDirectionChanges() {
        // TODO Auto-generated method stub
        return 0;
    }

}
