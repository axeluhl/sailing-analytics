package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseAreaChangeEventImpl extends RaceLogEventImpl implements
RaceLogCourseAreaChangedEvent {
    private static final long serialVersionUID = -3943108136024977860L;

    private final Serializable courseAreaId;

    public RaceLogCourseAreaChangeEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Serializable courseAreaId) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.courseAreaId = courseAreaId;
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
