package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sse.common.UtilNew;

public interface SpeedWithBearingWithConfidence<RelativeTo> extends
        HasConfidence<UtilNew.Triple<Speed, Double, Double>, SpeedWithBearing, RelativeTo> {
    SpeedWithBearing getObject();
}
