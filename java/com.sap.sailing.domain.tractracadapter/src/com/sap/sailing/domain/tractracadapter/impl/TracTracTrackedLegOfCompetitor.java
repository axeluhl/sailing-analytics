package com.sap.sailing.domain.tractracadapter.impl;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.tractrac.ResultAPI.LiveResultItem;
import com.tractrac.ResultAPI.MarkResultItem;

public class TracTracTrackedLegOfCompetitor implements TrackedLegOfCompetitor {
    private final TracTracTrackedRaceImpl trackedRace;
    private final Leg leg;
    private final Competitor competitor;
    
    public TracTracTrackedLegOfCompetitor(TracTracTrackedRaceImpl trackedRace, Leg leg, Competitor competitor) {
        this.trackedRace = trackedRace;
        this.leg = leg;
        this.competitor = competitor;
    }
    
    private MarkResultItem getMarkResults(TimePoint timePoint) {
        return getTrackedRace().getCompetitorMarkResults(getCompetitor(), getLeg().getFrom(), timePoint);
    }

    private LiveResultItem getLiveResults(TimePoint timePoint) {
        return getTrackedRace().getCompetitorLiveResults(getCompetitor(), timePoint);
    }

    @Override
    public Leg getLeg() {
        return leg;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public long getTimeInMilliSeconds(TimePoint timePoint) {
        return getMarkResults(timePoint).getLegtime();
    }

    @Override
    public Distance getDistanceTraveled(TimePoint timePoint) {
        return new NauticalMileDistance(getMarkResults(timePoint).getTraveledDistance() / Mile.METERS_PER_NAUTICAL_MILE);
    }

    @Override
    public Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException {
        return new NauticalMileDistance(getLiveResults(timePoint).getDistNextMark() / Mile.METERS_PER_NAUTICAL_MILE);
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        Position approximateLegStartPosition = getTrackedRace().getOrCreateTrack(
                getLeg().getFrom().getBuoys().iterator().next()).getEstimatedPosition(timePoint, false);
        Position approximateLegEndPosition = getTrackedRace().getOrCreateTrack(
                getLeg().getTo().getBuoys().iterator().next()).getEstimatedPosition(timePoint, false);
        Bearing bearing = approximateLegStartPosition.getBearingGreatCircle(approximateLegEndPosition);
        return new KnotSpeedWithBearingImpl(getMarkResults(timePoint).getVMG(), bearing);
    }

    @Override
    public Speed getAverageSpeedOverGround(TimePoint timePoint) {
        return new KnotSpeedImpl(getMarkResults(timePoint).getSOG());
    }

    @Override
    public Speed getMaximumSpeedOverGround(TimePoint timePoint) {
        return new KilometersPerHourSpeedImpl(getMarkResults(timePoint).getTopspeed());
    }

    @Override
    public Integer getNumberOfTacks(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Integer getNumberOfJibes(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Integer getNumberOfPenaltyCircles(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRank(TimePoint timePoint) {
        return getLiveResults(timePoint).getRank();
    }

    @Override
    public Double getGapToLeaderInSeconds(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0.0;
    }

    @Override
    public boolean hasStartedLeg(TimePoint timePoint) {
        MarkPassing markPassingForLegStart = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getFrom());
        return markPassingForLegStart != null && markPassingForLegStart.getTimePoint().compareTo(timePoint) <= 0;
    }

    @Override
    public boolean hasFinishedLeg(TimePoint timePoint) {
        MarkPassing markPassingForLegEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
        return markPassingForLegEnd != null && markPassingForLegEnd.getTimePoint().compareTo(timePoint) <= 0;
    }

    @Override
    public Speed getVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        return new KnotSpeedWithBearingImpl(getLiveResults(timePoint).getVmgAverage30s(),
                new DegreeBearingImpl(getLiveResults(timePoint).getHeading()));
    }

    @Override
    public Double getEstimatedTimeToNextMarkInSeconds(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0.0;
    }

    private TracTracTrackedRaceImpl getTrackedRace() {
        return trackedRace;
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
    public Distance getWindwardDistanceToOverallLeader(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Maneuver> getManeuvers(TimePoint timePoint) throws NoWindException {
        List<Maneuver> result = null;
        if (!hasStartedLeg(timePoint)) {
            MarkPassing markPassingForLegStart = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getFrom());
            MarkPassing legEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
            TimePoint end = timePoint;
            if (legEnd != null && timePoint.compareTo(legEnd.getTimePoint()) > 0) {
                // timePoint is after leg finish; take leg end and end time point
                end = legEnd.getTimePoint();
            }
            result = getTrackedRace().getManeuvers(competitor, markPassingForLegStart.getTimePoint(), end);
        }
        return result;
    }

}
