package com.sap.sailing.domain.common.orc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sse.common.Util;

public class ORCPerformanceCurveCourseImpl implements ORCPerformanceCurveCourse {
    private static final long serialVersionUID = -8975425405727779768L;
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
        return subcourse(lastFinishedLegOneBased, shareOfCurrentLeg, /* windwardLeewardLegReplacer */ null);
    }
    
    @Override
    public ORCPerformanceCurveCourse subcourse(int lastFinishedLegOneBased, double shareOfCurrentLeg,
            BiFunction<Integer, ORCPerformanceCurveLeg, ORCPerformanceCurveLeg> windwardLeewardLegReplacer) {
        // does function for empty courses, returns again empty course
        if (lastFinishedLegOneBased >= Util.size(legs)) {
            return this;
        } else {
            List<ORCPerformanceCurveLeg> resultLegs = new ArrayList<>();
            int count = 0;
            ORCPerformanceCurveLeg lastFinishedLeg = null;
            for (final ORCPerformanceCurveLeg leg : getLegs()) {
                final ORCPerformanceCurveLeg potentiallyReplacedLeg;
                if (leg.getType() == ORCPerformanceCurveLegTypes.WINDWARD_LEEWARD_REAL_LIVE && windwardLeewardLegReplacer != null) {
                    potentiallyReplacedLeg = windwardLeewardLegReplacer.apply(count, leg);
                } else {
                    potentiallyReplacedLeg = leg;
                }
                if (count++ >= lastFinishedLegOneBased) {
                    lastFinishedLeg = potentiallyReplacedLeg;
                    break;
                }
                resultLegs.add(potentiallyReplacedLeg);
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
