package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.TimePoint;

public interface Race extends WithDescription {
    String getRaceID();
    
    TimePoint getStartTime();

    void setStartTime(TimePoint timePoint);
}
