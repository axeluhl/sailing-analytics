package com.sap.sailing.domain.igtimiadapter;

import com.sap.sse.common.TimePoint;

public interface HasStartAndEndTime {
    TimePoint getEndTime();

    TimePoint getStartTime();
}
