package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class CourseChangeImpl extends GPSFixMovingImpl implements CourseChange {
    private final double courseChangeInDegrees;
    private final double speedChangeInKnots;
    
    public CourseChangeImpl(double courseChangeInDegrees, double speedChangeInKnots, Position position,
            TimePoint timePoint, SpeedWithBearing from) {
        super(position, timePoint, from);
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

}
