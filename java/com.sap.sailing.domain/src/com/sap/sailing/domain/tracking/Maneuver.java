package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;

public interface Maneuver extends GPSFix {
    ManeuverType getType();
    
    Tack getNewTack();
    
    SpeedWithBearing getSpeedWithBearingBefore();
    
    SpeedWithBearing getSpeedWithBearingAfter();
    
    double getDirectionChangeInDegrees();

    Distance getManeuverLoss();
    
}
