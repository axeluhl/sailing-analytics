package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface Boat extends Named {
    BoatClass getBoatClass();
    
    String getSailID();
}
