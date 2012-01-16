package com.sap.sailing.domain.base;

import com.sap.sailing.domain.confidence.HasConfidence;

public interface SpeedWithConfidence extends HasConfidence<SpeedWithConfidence, SpeedWithConfidence> {
    Speed getSpeed();
}
