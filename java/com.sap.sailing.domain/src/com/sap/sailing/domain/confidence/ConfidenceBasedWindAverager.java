package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.ScalableWind;

public interface ConfidenceBasedWindAverager<RelativeTo> extends ConfidenceBasedAverager<ScalableWind, Wind, RelativeTo>{
}
