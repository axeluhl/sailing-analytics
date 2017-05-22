package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseAreaChangeEventImpl extends RaceLogEventImpl implements RaceLogCourseAreaChangedEvent {
    private static final long serialVersionUID = -3943108136024977860L;

    private final Serializable courseAreaId;

    public RaceLogCourseAreaChangeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, Serializable courseAreaId) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.courseAreaId = courseAreaId;
    }

    public RaceLogCourseAreaChangeEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            Serializable courseAreaId) {
        this(now(), logicalTimePoint, author, randId(), pPassId, courseAreaId);
    }

    @Override
    public Serializable getCourseAreaId() {
        return courseAreaId;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
