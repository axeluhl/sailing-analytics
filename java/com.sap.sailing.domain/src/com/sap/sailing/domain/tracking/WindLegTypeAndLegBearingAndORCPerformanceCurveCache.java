package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.orc.ORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;

public interface WindLegTypeAndLegBearingAndORCPerformanceCurveCache
        extends WindCache, LegTypeCache, LegBearingCache, ORCPerformanceCurveCache {
    MarkPositionAtTimePointCache getMarkPositionAtTimePointCache(TimePoint markPositionTimePoint,
            TrackedRace trackedRace);
}
