package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface Moving {
    @SideEffectFreeValue(messageKey="Speed")
    SpeedWithBearing getSpeed();
}
