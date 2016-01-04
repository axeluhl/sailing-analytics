package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;

public interface GPSFixReceivedListener {
    void fixReceived(DeviceIdentifier device, GPSFix fix);
}
