package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.domain.confidence.HasConfidence;

public interface SpeedWithBearingWithConfidence extends
        HasConfidence<Triple<Speed, Double, Double>, SpeedWithBearingWithConfidence> {
    SpeedWithBearing getSpeedWithBearing();
}
