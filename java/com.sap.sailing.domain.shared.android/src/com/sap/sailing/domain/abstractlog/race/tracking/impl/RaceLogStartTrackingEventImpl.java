package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogStartTrackingEventImpl extends RaceLogEventImpl implements
RaceLogStartTrackingEvent {
    private static final long serialVersionUID = 6017954368580125221L;

    public RaceLogStartTrackingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int pPassId) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
    }

    public RaceLogStartTrackingEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int pPassId) {
        super(logicalTimePoint, author, pPassId);
    }


    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
