package com.sap.sailing.domain.orc;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public interface ORCPerformanceCurveCache {
    /**
     * If not yet computed, computes a copy of the total course supplied by the {@code totalCourseSupplier} where
     * any adapted leg that would query a live {@link TrackedLeg} for TWA and distance is replaced by a leg of
     * type {@link ORCPerformanceCurveLegImpl} of type {@link ORCPerformanceCurveLegTypes#TWA} with its TWA and
     * distance set to the values obtained for this cache's time point, computed once when this call is made
     * and no total course has been cached in this object yet.
     */
    ORCPerformanceCurveCourse getTotalCourse(TrackedRace raceContext, Supplier<ORCPerformanceCurveCourse> totalCourseSupplier);
    
    /**
     * Cache key is the pair consisting of {@code timePoint} and {@code raceContext}. A cache miss will
     * lead to the {@code scratchBoatSupplier} being applied with the {@code timePoint} passed.
     */
    Competitor getScratchBoat(TimePoint timePoint, TrackedRace raceContext, Function<TimePoint, Competitor> scratchBoatSupplier);

    /**
     * @param timePoint
     *            if this differs from the general time point this cache is for, or if no value for the general time
     *            point has been computed yet, the {@code performanceCurveSupplier} is used to compute it. Otherwise, the
     *            cached value is returned, using the {@code raceContext} and the {@code competitor} as the key.
     */
    ORCPerformanceCurve getPerformanceCurveForPartialCourse(TimePoint timePoint, TrackedRace raceContext,
            Competitor competitor, BiFunction<TimePoint, Competitor, ORCPerformanceCurve> performanceCurveSupplier);
    
    /**
     * @param timePoint
     *            if this differs from the general time point this cache is for, or if no value for the general time
     *            point has been computed yet, the {@code performanceCurveSupplier} is used to compute it. Otherwise, the
     *            cached value is returned, using the {@code raceContext} and the {@code competitor} as the key.
     */
    Speed getImpliedWind(TimePoint timePoint, TrackedRace raceContext, Competitor competitor,
            BiFunction<TimePoint, Competitor, Speed> impliedWindSupplier);

}
