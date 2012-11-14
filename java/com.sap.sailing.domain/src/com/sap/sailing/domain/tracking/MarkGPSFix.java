package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SingleMark;

public interface MarkGPSFix extends GPSFixMoving {
    SingleMark getMark();
}
