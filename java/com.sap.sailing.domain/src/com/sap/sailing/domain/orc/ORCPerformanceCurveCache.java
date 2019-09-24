package com.sap.sailing.domain.orc;

import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sse.common.Speed;

public interface ORCPerformanceCurveCache {
    /**
     * If not yet computed, computes a copy of the total course supplied by the {@code totalCourseSupplier} where
     * any adapted leg that would query a live {@link TrackedLeg} for TWA and distance is replaced by a leg of
     * type {@link ORCPerformanceCurveLegImpl} of type {@link ORCPerformanceCurveLegTypes#TWA} with its TWA and
     * distance set to the values obtained for this cache's time point, computed once when this call is made
     * and no total course has been cached in this object yet.
     */
    ORCPerformanceCurveCourse getTotalCourse(Supplier<ORCPerformanceCurveCourse> totalCourseSupplier);
    
    Competitor getScratchBoat(Supplier<Competitor> scratchBoatSupplier);

    ORCPerformanceCurve getPerformanceCurveForPartialCourse(Competitor competitor, Function<Competitor, ORCPerformanceCurve> performanceCurveSupplier);
    
    Speed getImpliedWind(Competitor competitor, Function<Competitor, Speed> impliedWindSupplier);

}
