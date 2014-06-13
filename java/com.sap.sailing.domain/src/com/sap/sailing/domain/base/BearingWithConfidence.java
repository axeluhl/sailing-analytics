package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.confidence.HasConfidenceAndIsScalable;


public interface BearingWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<DoublePair, Bearing, RelativeTo> {
}
