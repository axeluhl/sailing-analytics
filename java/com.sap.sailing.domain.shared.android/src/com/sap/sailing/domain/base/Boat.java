package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface Boat extends Named {
    BoatClass getBoatClass();
    
    @Dimension(messageKey="SailID")
    String getSailID();
}
