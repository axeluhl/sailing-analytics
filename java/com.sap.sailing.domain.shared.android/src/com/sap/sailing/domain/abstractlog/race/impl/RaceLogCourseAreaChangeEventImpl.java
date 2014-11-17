package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class RaceLogCourseAreaChangeEventImpl extends RaceLogEventImpl implements
RaceLogCourseAreaChangedEvent {
    private static final long serialVersionUID = -3943108136024977860L;

    private final Serializable courseAreaId;

    public RaceLogCourseAreaChangeEventImpl(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Serializable courseAreaId) {
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
