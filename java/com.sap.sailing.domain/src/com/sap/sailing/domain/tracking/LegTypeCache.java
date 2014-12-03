package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sse.common.TimePoint;

/**
 * When comprehensive calculations are prone to query the leg type several times for the same leg for the same time
 * point, a cache of this type can be used to eliminate repeated evaluation of common sub-expressions that are expensive
 * to calculate.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface LegTypeCache {
    LegType getLegType(TrackedLeg trackedLeg, TimePoint timePoint) throws NoWindException;
}
