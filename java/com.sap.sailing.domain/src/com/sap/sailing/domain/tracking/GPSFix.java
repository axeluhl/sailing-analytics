package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.SpeedWithBearing;

public interface GPSFix extends Positioned, Timed, WithValidityCache {
    SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to);
}