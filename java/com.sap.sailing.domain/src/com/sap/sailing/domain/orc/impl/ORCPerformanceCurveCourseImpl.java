package com.sap.sailing.domain.orc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.orc.ORCPerformanceCurveLeg;
import com.sap.sse.common.Distance;

public class ORCPerformanceCurveCourseImpl implements ORCPerformanceCurveCourse {

    private List<ORCPerformanceCurveLeg> legs;
    private Distance totalLength;

    public ORCPerformanceCurveCourseImpl(List<ORCPerformanceCurveLeg> legs) {
        this.legs = Collections.unmodifiableList(legs);
        totalLength = new NauticalMileDistance(0);

        for (ORCPerformanceCurveLeg leg : legs) {
            totalLength = new NauticalMileDistance(totalLength.getNauticalMiles() + leg.getLength().getNauticalMiles());
        }
    }

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

    @Override
    public ORCPerformanceCurveCourse subcourse(int lastFinishedLeg, double perCentOfCurrentLeg) {
        if (lastFinishedLeg == 0) {
            ORCPerformanceCurveLeg resultLeg = legs.get(0);
            List<ORCPerformanceCurveLeg> resultLegs = new ArrayList<>();
            resultLegs.add(new ORCPerformanceCurveLegImpl(resultLeg.getLength().scale(perCentOfCurrentLeg), resultLeg.getTwa()));
            return new ORCPerformanceCurveCourseImpl(resultLegs);
        }
        else if (lastFinishedLeg >= legs.size()) {
            return this;
        } else {
            List<ORCPerformanceCurveLeg> resultLegs = new ArrayList<>();
            resultLegs.addAll(legs.subList(0, lastFinishedLeg));
            resultLegs.add(new ORCPerformanceCurveLegImpl(legs.get(lastFinishedLeg).getLength().scale(perCentOfCurrentLeg), legs.get(lastFinishedLeg).getTwa()));
            return new ORCPerformanceCurveCourseImpl(resultLegs);
        }
    }
    
}
