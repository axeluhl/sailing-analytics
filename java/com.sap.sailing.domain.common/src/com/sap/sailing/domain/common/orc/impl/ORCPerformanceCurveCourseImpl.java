package com.sap.sailing.domain.common.orc.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sse.common.Util;

public class ORCPerformanceCurveCourseImpl implements ORCPerformanceCurveCourse {
    private final Iterable<ORCPerformanceCurveLeg> legs;

    public ORCPerformanceCurveCourseImpl(Iterable<ORCPerformanceCurveLeg> legs) {
        this.legs = legs;
    }

    @Override
    public Iterable<ORCPerformanceCurveLeg> getLegs() {
        return legs;
    }

    @Override
    public ORCPerformanceCurveCourse subcourse(int lastFinishedLegOneBased, double shareOfCurrentLeg) {
        // does function for empty courses, returns again empty course
        if (lastFinishedLegOneBased >= Util.size(legs)) {
            return this;
        } else {
            List<ORCPerformanceCurveLeg> resultLegs = new ArrayList<>();
            int count = 0;
            ORCPerformanceCurveLeg lastFinishedLeg = null;
            for (final ORCPerformanceCurveLeg leg : getLegs()) {
                if (count++ >= lastFinishedLegOneBased) {
                    lastFinishedLeg = leg;
                    break;
                }
                resultLegs.add(leg);
            }
            resultLegs.add(lastFinishedLeg.scale(shareOfCurrentLeg));
            return new ORCPerformanceCurveCourseImpl(resultLegs);
        }
    }

    @Override
    public String toString() {
        return "[legs=" + legs + ", totalLength=" + getTotalLength().getNauticalMiles() + "NM]";
    }
}
