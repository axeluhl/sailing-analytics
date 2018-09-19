package com.sap.sailing.windestimation.maneuvergraph;

public interface AssumedWindCourseWithTransition extends AssumedWindCourse {

    double getPenaltyFactorForTransition(double secondsPassed);

}
