package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.domain.confidence.HasConfidence;

public interface SpeedWithBearingWithConfidence extends SpeedWithBearing,
        HasConfidence<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> {
}
