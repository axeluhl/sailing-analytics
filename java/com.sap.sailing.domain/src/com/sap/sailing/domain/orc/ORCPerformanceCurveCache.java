package com.sap.sailing.domain.orc;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.AverageWindOnLegCache;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public interface ORCPerformanceCurveCache extends AverageWindOnLegCache {
    /**
     * If not yet computed, computes a copy of the total course supplied by the {@code totalCourseSupplier} where any
     * adapted leg that would query a live {@link TrackedLeg} for TWA and distance is replaced by a leg of type
     * {@link ORCPerformanceCurveLegImpl} of type {@link ORCPerformanceCurveLegTypes#TWA} with its TWA and distance set
     * to the values obtained for this cache's time point, computed once when this call is made and no total course for
     * the {@code raceContext} has been cached in this object yet.
     * 
     * @param totalCourseSupplier
     *            computes the {@link ORCPerformanceCurveCourse} representing the total course for race represented by
     *            {@code raceContext}.
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

    /**
     * Computes the (relative) corrected time by determining the implied wind to use at {@code timePoint} (the greatest
     * found for any competitor), then determining the {@code competitor}'s performance curve for the partial or total
     * course sailed up to {@code timePoint} and mapping the implied wind to the time allowance for {@code competitor}
     * at {@code timePoint}. This allowance is then compared to the time sailed by {@code competitor} at
     * {@code timePoint}, and the difference is returned. Note that this difference could be negative, e.g., in case
     * the implied wind calculation was capped at 20 knots, but the boat sailed faster in an actual 25 knots breeze,
     * therefore having an elapsed time that is shorter than the allowance at 20 knots.
     */
    Duration getRelativeCorrectedTime(Competitor competitor, TrackedRace raceContext,
            TimePoint timePoint, BiFunction<Competitor, TimePoint, Duration> relativeCorrectedTimeSupplier);
}