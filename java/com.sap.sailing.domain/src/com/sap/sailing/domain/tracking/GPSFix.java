package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;

public interface GPSFix extends Positioned, Timed, WithValidityCache {
    SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to);
}