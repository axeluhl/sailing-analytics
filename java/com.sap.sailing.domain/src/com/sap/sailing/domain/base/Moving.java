package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface Moving {
    @Connector
    SpeedWithBearing getSpeed();
}
