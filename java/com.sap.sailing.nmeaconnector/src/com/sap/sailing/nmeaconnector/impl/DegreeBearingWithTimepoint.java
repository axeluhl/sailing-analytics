package com.sap.sailing.nmeaconnector.impl;

import com.sap.sailing.nmeaconnector.TimedBearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class DegreeBearingWithTimepoint extends DegreeBearingImpl implements TimedBearing {
    private static final long serialVersionUID = 8704478920647067862L;
    private final TimePoint timePoint;

    public DegreeBearingWithTimepoint(TimePoint timePoint, double bearingDeg) {
        super(bearingDeg);
        this.timePoint = timePoint;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }
}
