package com.sap.sailing.domain.base;

import com.sap.sailing.domain.confidence.HasConfidence;

public interface SpeedWithConfidence extends Speed, HasConfidence<SpeedWithConfidence, SpeedWithConfidence> {
}
