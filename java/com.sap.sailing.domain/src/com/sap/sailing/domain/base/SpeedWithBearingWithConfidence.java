package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sse.common.Util;

public interface SpeedWithBearingWithConfidence<RelativeTo> extends
        HasConfidence<Util.Triple<Speed, Double, Double>, SpeedWithBearing, RelativeTo> {
    SpeedWithBearing getObject();
}
