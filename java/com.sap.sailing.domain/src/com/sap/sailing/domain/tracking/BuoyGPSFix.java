package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Buoy;

public interface BuoyGPSFix extends GPSFixMoving {
    Buoy getBuoy();
}
