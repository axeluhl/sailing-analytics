package com.sap.sailing.domain.common.abstractlog;

import com.sap.sse.common.TimePoint;

public class TimePointSpecificationFoundInLogImpl implements TimePointSpecificationFoundInLog {
    private static final long serialVersionUID = -5499989656571658254L;
    private TimePoint timePoint;

    TimePointSpecificationFoundInLogImpl() {} // for GWT serialization
    
    public TimePointSpecificationFoundInLogImpl(TimePoint timePoint) {
        super();
        this.timePoint = timePoint;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }
}
