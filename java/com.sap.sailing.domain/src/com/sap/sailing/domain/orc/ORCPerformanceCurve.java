package com.sap.sailing.domain.orc;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

public interface ORCPerformanceCurve {

    Speed getImpliedWind();
    
    Duration getCalculatedTime(ORCPerformanceCurve referenceBoat);
    
}
