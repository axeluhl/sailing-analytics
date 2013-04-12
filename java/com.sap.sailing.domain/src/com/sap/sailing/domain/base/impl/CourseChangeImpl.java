package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CourseChange;

public class CourseChangeImpl implements CourseChange {
    private final double courseChangeInDegrees;
    private final double speedChangeInKnots;
    
    public CourseChangeImpl(double courseChangeInDegrees, double speedChangeInKnots) {
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
    public String toString() {
        return "changing course by "+getCourseChangeInDegrees()+"deg, "+getSpeedChangeInKnots()+"kn";
    }
}
