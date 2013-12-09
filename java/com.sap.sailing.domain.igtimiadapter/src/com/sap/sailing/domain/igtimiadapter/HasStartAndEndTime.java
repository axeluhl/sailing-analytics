package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.common.TimePoint;

public interface HasStartAndEndTime {
    TimePoint getEndTime();

    TimePoint getStartTime();
}
