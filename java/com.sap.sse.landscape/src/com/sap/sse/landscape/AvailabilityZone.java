package com.sap.sse.landscape;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

public interface AvailabilityZone extends Named, WithID {
    @Override
    String getId();
    
    Region getRegion();
}
