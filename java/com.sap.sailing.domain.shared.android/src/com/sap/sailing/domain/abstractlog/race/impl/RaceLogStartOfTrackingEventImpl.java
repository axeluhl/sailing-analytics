package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogStartOfTrackingEventImpl extends RaceLogEventImpl implements RaceLogStartOfTrackingEvent {
    private static final long serialVersionUID = -4134608549471817247L;

    public RaceLogStartOfTrackingEventImpl(TimePoint createdAt, TimePoint startOfTracking,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, startOfTracking, author, pId, pInvolvedBoats, pPassId);
    }

    public RaceLogStartOfTrackingEventImpl(TimePoint startOfTracking, AbstractLogEventAuthor author, int pPassId) {
        super(startOfTracking, author, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "startOfTrackingTime=" + getLogicalTimePoint();
    }
}
