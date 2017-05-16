package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseDesignChangedEventImpl extends RaceLogEventImpl implements RaceLogCourseDesignChangedEvent {
    private static final long serialVersionUID = 1565936170747420547L;

    private final CourseBase courseDesign;
    
    private final CourseDesignerMode courseDesignerMode;

    public RaceLogCourseDesignChangedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int pPassId,
            CourseBase courseDesign, CourseDesignerMode courseDesignerMode) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.courseDesign = courseDesign;
        this.courseDesignerMode = courseDesignerMode;
    }

    public RaceLogCourseDesignChangedEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            CourseBase courseDesign, CourseDesignerMode courseDesignerMode) {
        this(now(), logicalTimePoint, author, randId(), pPassId, courseDesign, courseDesignerMode);
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
    public CourseDesignerMode getCourseDesignerMode() {
        return courseDesignerMode;
    }

    @Override
    public String getShortInfo() {
        return "courseDesign=" + courseDesign != null ? courseDesign.getName() : "null";
    }

}