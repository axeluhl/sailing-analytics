package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Timed;

public interface SensorFix extends Timed {
    double get(String valueName);
}
