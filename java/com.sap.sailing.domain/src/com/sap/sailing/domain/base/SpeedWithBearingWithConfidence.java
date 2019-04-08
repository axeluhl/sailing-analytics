package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.DoubleTriple;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.confidence.HasConfidence;

public interface SpeedWithBearingWithConfidence<RelativeTo> extends
        HasConfidence<DoubleTriple, SpeedWithBearing, RelativeTo> {
    SpeedWithBearing getObject();
}
