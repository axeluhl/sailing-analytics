package com.sap.sailing.domain.common.orc;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public interface ORCPerformanceCurveLeg {

    Distance getLength();
    
    Bearing getTwa();
    
    String toString();
    
    ORCPerformanceCurveLegTypes getType();
}
