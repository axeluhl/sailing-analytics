package com.sap.sailing.domain.orc;

import java.util.List;

import com.sap.sse.common.Distance;

public interface ORCPerformanceCurveCourse {

    //TODO Return unmodifiable List/Collection something...
    Iterable<ORCPerformanceCurveLeg> getLegs();
    
    default Distance getTotalLength() {
        Distance result = Distance.NULL;
        for (ORCPerformanceCurveLeg leg : getLegs()) {
            result = result.add(leg.getLength());
        }
        return result;
    }
    
}
