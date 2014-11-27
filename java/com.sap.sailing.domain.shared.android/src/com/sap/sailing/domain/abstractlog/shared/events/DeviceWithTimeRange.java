package com.sap.sailing.domain.abstractlog.shared.events;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;

public interface DeviceWithTimeRange extends Timed {
    DeviceIdentifier getDevice();

    TimeRange getTimeRange();
}
