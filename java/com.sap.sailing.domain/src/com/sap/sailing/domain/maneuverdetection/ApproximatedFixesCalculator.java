package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;

/**
 * Calculates douglas peucker fixes.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ApproximatedFixesCalculator {

    /**
     * Approximates douglas peucker points within the provided time range.
     */
    Iterable<GPSFixMoving> approximate(TimePoint earliestStart, TimePoint latestEnd);

}
