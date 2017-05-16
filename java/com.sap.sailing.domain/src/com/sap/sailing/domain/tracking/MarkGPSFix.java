package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;

public interface MarkGPSFix extends GPSFixMoving {
    Mark getMark();
}
