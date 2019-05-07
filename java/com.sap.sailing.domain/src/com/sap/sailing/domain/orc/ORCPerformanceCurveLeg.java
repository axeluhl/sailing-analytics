package com.sap.sailing.domain.orc;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public interface ORCPerformanceCurveLeg {

    Distance getLength();
    
    Bearing getTwa();
    
}
