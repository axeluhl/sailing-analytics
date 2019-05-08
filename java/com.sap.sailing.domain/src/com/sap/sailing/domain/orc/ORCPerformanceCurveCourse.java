package com.sap.sailing.domain.orc;

import java.util.List;

import com.sap.sse.common.Distance;

public interface ORCPerformanceCurveCourse {

    ORCPerformanceCurveLeg getLeg(int i);
    
    List<ORCPerformanceCurveLeg> getLegs();
    
    Distance getTotalLength();
    
}
