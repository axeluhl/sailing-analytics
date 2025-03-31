package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.confidence.HasConfidenceAndIsScalable;
import com.sap.sse.common.Speed;

public interface SpeedWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<Double, Speed, RelativeTo> {
}
