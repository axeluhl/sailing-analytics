package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;

public class RaceLogCourseDesignChangedEventImpl extends RaceLogEventImpl implements RaceLogCourseDesignChangedEvent {
    private static final long serialVersionUID = 1565936170747420547L;
   
    private CourseData courseDesign;

    public RaceLogCourseDesignChangedEventImpl(TimePoint createdAt,
            TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, CourseData courseDesign) {
        super(createdAt, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.courseDesign = courseDesign;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CourseData getCourseDesign() {
        return courseDesign;
    }

}
