package com.sap.sailing.domain.common.confidence;

import com.sap.sailing.domain.common.DoublePair;
import com.sap.sse.common.Bearing;


public interface BearingWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<DoublePair, Bearing, RelativeTo> {
}
