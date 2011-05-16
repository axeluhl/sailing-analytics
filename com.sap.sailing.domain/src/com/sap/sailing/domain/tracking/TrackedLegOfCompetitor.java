package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;

public interface TrackedLegOfCompetitor extends ForCompetitor {
    Leg getLeg();

    long getTimeInMilliSeconds();

    Distance getDistanceTraveled();

    Speed getAverageVelocityMadeGood();

    Speed getAverageSpeedOverGround();

    Speed getMaximumSpeedOverGround();
    
    int getNumberOfTacks();
    
    int getNumberOfJibes();
    
    int getNumberOfDirectionChanges();
    
    Iterable<BoatGPSFix> getGPSFixes();
}
