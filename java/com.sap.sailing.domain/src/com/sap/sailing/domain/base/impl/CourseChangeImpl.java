package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;

public class CourseChangeImpl extends GPSFixImpl implements CourseChange {
    private final double courseChangeInDegrees;
    private final double speedChangeInKnots;
    
    public CourseChangeImpl(double courseChangeInDegrees, double speedChangeInKnots, Position position, TimePoint timePoint) {
        super(position, timePoint);
        this.courseChangeInDegrees = courseChangeInDegrees;
        this.speedChangeInKnots = speedChangeInKnots;
    }
    
    @Override
    public double getCourseChangeInDegrees() {
        return courseChangeInDegrees;
    }

    @Override
    public double getSpeedChangeInKnots() {
        return speedChangeInKnots;
    }

    @Override
    public Tack to() {
        return getCourseChangeInDegrees() > 0 ? Tack.STARBOARD : getCourseChangeInDegrees() < 0 ? Tack.PORT : null;
    }

}
