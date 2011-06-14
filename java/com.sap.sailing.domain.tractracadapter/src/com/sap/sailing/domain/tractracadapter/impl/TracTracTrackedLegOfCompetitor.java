package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
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
        Position approximateLegStartPosition = getTrackedRace().getTrack(
                getLeg().getFrom().getBuoys().iterator().next()).getEstimatedPosition(timePoint, false);
        Position approximateLegEndPosition = getTrackedRace().getTrack(
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
        // TODO Auto-generated method stub; problem: live VMG is not delivered by new ResultAPI
        return null;
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

}
