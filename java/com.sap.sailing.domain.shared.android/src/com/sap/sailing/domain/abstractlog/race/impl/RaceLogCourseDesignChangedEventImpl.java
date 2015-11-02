package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseDesignChangedEventImpl extends RaceLogEventImpl implements RaceLogCourseDesignChangedEvent {
    private static final long serialVersionUID = 1565936170747420547L;

    private final CourseBase courseDesign;

    public RaceLogCourseDesignChangedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId,
            CourseBase courseDesign) {
        super(createdAt, pTimePoint, author, pId, pInvolvedBoats, pPassId);
        this.courseDesign = courseDesign;
    }

    public RaceLogCourseDesignChangedEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId,
            CourseBase courseDesign) {
        super(pTimePoint, author, pPassId);
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
