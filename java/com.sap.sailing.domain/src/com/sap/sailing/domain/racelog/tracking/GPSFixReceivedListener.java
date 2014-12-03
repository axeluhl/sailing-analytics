package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;

public interface GPSFixReceivedListener {
    void fixReceived(DeviceIdentifier device, GPSFix fix);
}
