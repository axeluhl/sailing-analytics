package com.sap.sailing.domain.igtimiadapter.datatypes;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public abstract class Fix {
    private final TimePoint timePoint;
    private final Sensor sensor;

    protected Fix(Sensor sensor, TimePoint timePoint) {
        super();
        this.timePoint = timePoint;
        this.sensor = sensor;
    }

    public TimePoint getTimePoint() {
        return timePoint;
    }

    public Sensor getSensor() {
        return sensor;
    }
    
    abstract protected String localToString();
    
    @Override
    public String toString() {
        return localToString() + " at "+getTimePoint()+" from "+getSensor();
    }
}
