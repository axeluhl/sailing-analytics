package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((timepoint == null) ? 0 : timepoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        WindImpl other = (WindImpl) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (timepoint == null) {
            if (other.timepoint != null)
                return false;
        } else if (!timepoint.equals(other.timepoint))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ""+getTimePoint()+"@"+getPosition()+": "+getKnots()+"kn from "+getFrom();
    }
    
}
