package com.sap.sailing.domain.orc;

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
    
    // 0 => no legs finished, 1 => 1 leg finished, ... ; not equal to the index of the finished leg in a list
    ORCPerformanceCurveCourse subcourse(int lastFinishedLeg, double perCentOfCurrentLeg);
}
