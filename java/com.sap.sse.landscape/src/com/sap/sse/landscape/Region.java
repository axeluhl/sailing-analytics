package com.sap.sse.landscape;

import com.sap.sse.common.WithID;

public interface Region extends WithID {
    @Override
    String getId();
    
    Iterable<AvailabilityZone> getAvailabilityZones();
    
    Iterable<SecurityGroup> getSecurityGroups();
}
