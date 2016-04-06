package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.Timed;

public interface FixReceivedListener<FixT extends Timed> {
    void fixReceived(DeviceIdentifier device, FixT fix);
}
