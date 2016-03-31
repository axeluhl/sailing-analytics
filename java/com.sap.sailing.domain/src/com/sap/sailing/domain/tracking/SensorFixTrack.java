package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.tracking.SensorFix;

public interface SensorFixTrack<FixT extends SensorFix> extends Track<FixT> {
    
    Iterable<String> getValueNames();

}
