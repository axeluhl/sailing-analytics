package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.HasConfidenceAndIsScalable;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.ScalableWind;

public interface ConfidenceBasedWindAverager<RelativeTo> extends ConfidenceBasedAverager<ScalableWind, Wind, RelativeTo>{
    @Override
    WindWithConfidence<RelativeTo> getAverage(
            Iterable<? extends HasConfidenceAndIsScalable<ScalableWind, Wind, RelativeTo>> values, RelativeTo at);
}
