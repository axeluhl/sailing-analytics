package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;

public interface Maneuver extends GPSFix {
    ManeuverType getType();
    
    Tack getNewTack();
    
    SpeedWithBearing getSpeedWithBearingBefore();
    
    SpeedWithBearing getSpeedWithBearingAfter();
    
    double getDirectionChangeInDegrees();

    Distance getManeuverLoss();
    
}
