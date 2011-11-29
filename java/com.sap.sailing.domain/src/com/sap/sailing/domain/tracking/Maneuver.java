package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;

public interface Maneuver extends GPSFix {
    enum Type { HEAD_UP, BEAR_AWAY, TACK, JIBE, PENALTY_CIRCLE }
    
    Type getType();
    
    SpeedWithBearing getSpeedWithBearingBefore();
    
    SpeedWithBearing getSpeedWithBearingAfter();
    
    double getDirectionChangeInDegrees();
    
}
