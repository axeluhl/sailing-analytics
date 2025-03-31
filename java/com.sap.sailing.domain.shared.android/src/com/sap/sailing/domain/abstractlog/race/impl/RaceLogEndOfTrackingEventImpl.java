package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sse.common.TimePoint;

public class RaceLogEndOfTrackingEventImpl extends RaceLogEventImpl implements RaceLogEndOfTrackingEvent {
    private static final long serialVersionUID = 3130293807404190473L;

    public RaceLogEndOfTrackingEventImpl(TimePoint createdAt, TimePoint endOfTracking, AbstractLogEventAuthor author,
            Serializable pId, int pPassId) {
        super(createdAt, endOfTracking, author, pId, pPassId);
    }

    public RaceLogEndOfTrackingEventImpl(TimePoint endOfTracking, AbstractLogEventAuthor author, int pPassId) {
        super(endOfTracking, author, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "endOfTrackingTime=" + getLogicalTimePoint();
    }

}
