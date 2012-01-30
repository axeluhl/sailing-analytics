package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.confidence.HasConfidence;

public interface SpeedWithConfidence<RelativeTo> extends HasConfidence<Speed, Speed, RelativeTo> {
    Speed getObject();
}
