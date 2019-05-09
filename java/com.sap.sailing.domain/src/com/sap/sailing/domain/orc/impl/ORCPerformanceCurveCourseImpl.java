package com.sap.sailing.domain.orc.impl;

import java.util.List;

import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.orc.ORCPerformanceCurveLeg;
import com.sap.sse.common.Distance;

public class ORCPerformanceCurveCourseImpl implements ORCPerformanceCurveCourse {

    private List<ORCPerformanceCurveLeg> legs;
    private Distance totalLength;

    public ORCPerformanceCurveCourseImpl(List<ORCPerformanceCurveLeg> legs) {
        this.legs = legs;
        totalLength = new NauticalMileDistance(0);

        for (ORCPerformanceCurveLeg leg : legs) {
            totalLength = new NauticalMileDistance(totalLength.getNauticalMiles() + leg.getLength().getNauticalMiles());
        }
    }

    @Override
    public ORCPerformanceCurveLeg getLeg(int i) {
        return legs.get(i);
    }

    @Override
    public Distance getTotalLength() {
        return totalLength;
    }

    @Override
    public List<ORCPerformanceCurveLeg> getLegs() {
        return legs;
    }

}
