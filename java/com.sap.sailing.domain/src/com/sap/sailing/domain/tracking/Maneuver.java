package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;

public interface Maneuver extends GPSFix {
    enum Type { HEAD_UP, BEAR_AWAY, TACK, JIBE, PENALTY_CIRCLE, MARK_PASSING, UNKNOWN }
    
    Type getType();
    
    Tack getNewTack();
    
    SpeedWithBearing getSpeedWithBearingBefore();
    
    SpeedWithBearing getSpeedWithBearingAfter();
    
    double getDirectionChangeInDegrees();
    
}
