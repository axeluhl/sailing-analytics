package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

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

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public Leg getLeg() {
        return trackedLeg.getLeg();
    }

    @Override
    public long getTimeInMilliSeconds() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Distance getDistanceTraveled() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageVelocityMadeGood() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageSpeedOverGround() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getMaximumSpeedOverGround() {
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

    @Override
    public Iterable<GPSFixMoving> getGPSFixes() {
        // TODO Auto-generated method stub
        return null;
    }

}
