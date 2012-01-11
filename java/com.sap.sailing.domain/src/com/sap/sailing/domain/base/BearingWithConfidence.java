package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.confidence.HasConfidence;

public interface BearingWithConfidence extends Bearing, HasConfidence<Pair<Double, Double>, BearingWithConfidence> {
}
