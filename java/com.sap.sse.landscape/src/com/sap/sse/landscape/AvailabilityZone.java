package com.sap.sse.landscape;

import com.sap.sse.common.WithID;

public interface AvailabilityZone extends WithID {
    @Override
    String getId();
    
    Region getRegion();
}
