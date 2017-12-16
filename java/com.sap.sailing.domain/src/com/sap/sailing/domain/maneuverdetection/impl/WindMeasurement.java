package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.TimePoint;

class WindMeasurement {

    private final TimePoint timePoint;
    private final Position position;
    private final Bearing windCourse;

    public WindMeasurement(TimePoint timePoint, Position position, Bearing windCourse) {
        this.timePoint = timePoint;
        this.position = position;
        this.windCourse = windCourse;
    }

    public TimePoint getTimePoint() {
        return timePoint;
    }

    public Position getPosition() {
        return position;
    }
    
    public Bearing getWindCourse() {
        return windCourse;
    }
}
