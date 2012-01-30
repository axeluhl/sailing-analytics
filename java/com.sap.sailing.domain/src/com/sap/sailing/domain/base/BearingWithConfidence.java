package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.HasConfidenceAndIsScalable;


public interface BearingWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<Pair<Double, Double>, Bearing, RelativeTo> {
    Bearing getObject();
}
