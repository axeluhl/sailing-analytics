package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.AbstractSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class GPSFixMovingImpl extends GPSFixImpl implements GPSFixMoving {
    private final SpeedWithBearing speed;
    
    public GPSFixMovingImpl(Position position, TimePoint timePoint, SpeedWithBearing speed) {
        super(position, timePoint);
        this.speed = speed;
    }

    @Override
    public SpeedWithBearing getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return super.toString()+" with "+getSpeed();
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ getSpeed().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return super.equals(other) && other instanceof GPSFixMoving && getSpeed().equals(((GPSFixMoving) other).getSpeed());
    }

    @Override
    public CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing) {
        return AbstractSpeedWithBearingImpl.getCourseChangeRequiredToReach(getSpeed(), targetSpeedWithBearing, getPosition(), getTimePoint());
    }
}
