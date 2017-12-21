package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;

/**
 * Calculates douglas peucker points incrementally. The implementations must take care of concurrency, when state is
 * introduced.
 * 
 * @author Vladislav Chumak(D069712)
 *
 */
public interface IncrementalApproximatedFixesCalculator {

    /**
     * Approximates incrementally douglas peucker points within the provided time range. The implementation must
     * reproduce the call of
     * {@link com.sap.sailing.domain.tracking.TrackedRace#approximate(com.sap.sailing.domain.base.Competitor, com.sap.sailing.domain.common.Distance, TimePoint, TimePoint)}
     * supporting the incremental calculation.
     */
    Iterable<GPSFixMoving> approximate(TimePoint earliestStart, TimePoint latestEnd);

    /**
     * Clears the whole state of the calculator, which is used for incremental calculation. The following calls of
     * {@link #clearState()} will cause the this calculator to calculate douglas peucker fixes from scratch.
     */
    void clearState();

}
