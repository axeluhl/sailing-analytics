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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timePoint == null) ? 0 : timePoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimePointSpecificationFoundInLogImpl other = (TimePointSpecificationFoundInLogImpl) obj;
        if (timePoint == null) {
            if (other.timePoint != null)
                return false;
        } else if (!timePoint.equals(other.timePoint))
            return false;
        return true;
    }
}
