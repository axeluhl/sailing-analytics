package com.sap.sailing.domain.common.confidence;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.DoublePair;


public interface BearingWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<DoublePair, Bearing, RelativeTo> {
}
