package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;

public class SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator
        extends IntersectedWindRangeBasedTransitionProbabilitiesCalculator {

    public SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator(
            boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        super(propagateIntersectedWindRangeOfHeadupAndBearAway);
    }

    @Override
    protected WindCourseRange getJibeWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        WindCourseRange windRange = new WindCourseRange(middleCourse.getDegrees(), 0);
        return windRange;
    }

    @Override
    protected WindCourseRange getTackWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        WindCourseRange windRange = new WindCourseRange(middleCourse.reverse().getDegrees(), 0);
        return windRange;
    }

}
