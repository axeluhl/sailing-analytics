package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;

public class WindImpl extends KnotSpeedWithBearingImpl implements Wind {
    private static final long serialVersionUID = 5431592324949471980L;
    private final Position position;
    private final TimePoint timepoint;
    private final Bearing from;

    public WindImpl(Position p, TimePoint at, SpeedWithBearing windSpeedWithBearing) {
        super(windSpeedWithBearing.getKnots(), windSpeedWithBearing.getBearing());
        this.position = p;
        this.timepoint = at;
        double fromDeg = getBearing().getDegrees() + 180;
        if (fromDeg > 360) {
            fromDeg -= 360;
        }
        from = new DegreeBearingImpl(fromDeg);
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
        return from;
    }
    
    @Override
    public String toString() {
        return ""+getTimePoint()+"@"+getPosition()+": "+getKnots()+"kn from "+getFrom();
    }
    
}
