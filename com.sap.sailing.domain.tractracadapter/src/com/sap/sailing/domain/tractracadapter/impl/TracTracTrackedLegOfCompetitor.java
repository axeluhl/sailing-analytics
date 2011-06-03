package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TracTracTrackedLegOfCompetitor implements TrackedLegOfCompetitor {

    public TracTracTrackedLegOfCompetitor(TracTracTrackedRaceImpl trackedRace, Leg leg, Competitor competitor) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Leg getLeg() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Competitor getCompetitor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getTimeInMilliSeconds() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Distance getDistanceTraveled(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageSpeedOverGround(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getMaximumSpeedOverGround() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getGapToLeaderInSeconds(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasStartedLeg(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasFinishedLeg(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Speed getVelocityMadeGood(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getEstimatedTimeToNextMarkInSeconds(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return 0;
    }

}
