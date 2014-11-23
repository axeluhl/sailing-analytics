package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

public class WindImpl extends KnotSpeedWithBearingImpl implements Wind {
    private static final long serialVersionUID = 5431592324949471980L;
    private final Position position;
    private final TimePoint timepoint;

    public WindImpl(Position p, TimePoint at, SpeedWithBearing windSpeedWithBearing) {
        super(windSpeedWithBearing.getKnots(), windSpeedWithBearing.getBearing());
        this.position = p;
        this.timepoint = at;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TimePoint getTimePoint() {
        return timepoint;
    }

    @Override
    public Bearing getFrom() {
        return getBearing().reverse();
    }
    
    @Override
    public String toString() {
        return ""+getTimePoint()+"@"+getPosition()+": "+getKnots()+"kn from "+getFrom();
    }
    
}
