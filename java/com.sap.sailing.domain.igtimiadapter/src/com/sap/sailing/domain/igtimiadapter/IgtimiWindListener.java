package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.tracking.Wind;

public interface IgtimiWindListener {
    void windDataReceived(Wind wind, String deviceSerialNumber);
}
