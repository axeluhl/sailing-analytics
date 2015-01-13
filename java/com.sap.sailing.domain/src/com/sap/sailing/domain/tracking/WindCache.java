package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

/**
 * When comprehensive calculations are prone to query wind several times for the same competitor in the same race for
 * the same time point, a cache of this type can be used to eliminate repeated evaluation of common sub-expressions that
 * are expensive to calculate.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface WindCache {
    /**
     * Estimates the <code>competitor</code>'s position at <code>timePoint</code> and determines the wind at that position and
     * at that time.
     */
    Wind getWind(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint);
}
