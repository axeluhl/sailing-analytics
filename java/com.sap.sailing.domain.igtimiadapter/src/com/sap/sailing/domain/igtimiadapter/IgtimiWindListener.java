package com.sap.sailing.domain.igtimiadapter;

import java.util.Set;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

public interface IgtimiWindListener {
    void windDataReceived(Wind wind, Set<Fix> fixesUsed, String deviceSerialNumber);
}
