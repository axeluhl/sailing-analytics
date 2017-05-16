package com.sap.sailing.domain.base;

import com.sap.sse.common.Color;
import com.sap.sse.common.Named;
import com.sap.sse.datamining.annotations.Dimension;

public interface Boat extends Named {
    BoatClass getBoatClass();
    
    @Dimension(messageKey="SailID")
    String getSailID();
    
    Color getColor();
}
