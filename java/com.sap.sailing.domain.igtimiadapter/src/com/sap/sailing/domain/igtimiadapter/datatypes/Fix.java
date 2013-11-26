package com.sap.sailing.domain.igtimiadapter.datatypes;

import com.sap.sailing.domain.common.TimePoint;

public abstract class Fix {
    private final int code;
    private final TimePoint timePoint;

    protected Fix(int code, String sensorId, TimePoint timePoint) {
        super();
        this.code = code;
        this.timePoint = timePoint;
    }

    public int getCode() {
        return code;
    }

    public TimePoint getTimePoint() {
        return timePoint;
    }
}
