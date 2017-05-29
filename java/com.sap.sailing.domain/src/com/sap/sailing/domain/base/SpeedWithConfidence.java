package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.confidence.HasConfidenceAndIsScalable;

public interface SpeedWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<Double, Speed, RelativeTo> {
}
