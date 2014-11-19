package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseDesignChangedEventImpl extends RaceLogEventImpl implements RaceLogCourseDesignChangedEvent {
    private static final long serialVersionUID = 1565936170747420547L;
   
    private final CourseBase courseDesign;

    public RaceLogCourseDesignChangedEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, CourseBase courseDesign) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.courseDesign = courseDesign;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CourseBase getCourseDesign() {
        return courseDesign;
    }

    @Override
    public String getShortInfo() {
        return "courseDesign=" + courseDesign != null ? courseDesign.getName() : "null";
    }
}
