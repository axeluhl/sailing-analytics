package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;

public interface DeviceWithTimeRange extends Timed {
    DeviceIdentifier getDevice();

    TimeRange getTimeRange();
}
