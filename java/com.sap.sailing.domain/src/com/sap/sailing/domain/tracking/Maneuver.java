package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;

public interface Maneuver extends Timed, Positioned {
    enum Type { HEAD_UP, BEAR_AWAY, TACK, JIBE, PENALTY_CIRCLE }
    
    Type getType();
    
    SpeedWithBearing getSpeedWithBearingBefore();
    
    SpeedWithBearing getSpeedWithBearingAfter();
    
    double getDirectionChangeInDegrees();
    
    /**
     * This assumes that a maneuver usually slows a boat down. This method tries to find out how long
     * after the {@link #getTimePoint() time of the maneuver} it took for the boat to reach a roughly
     * constant speed again.
     */
    long getTimeInMillisecondsUntilBackToConstantSpeed();
    
}
