package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.HasConfidence;


public interface BearingWithConfidence<RelativeTo> extends HasConfidence<Pair<Double, Double>, Bearing, RelativeTo> {
    Bearing getObject();
}
