package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogStartOfTrackingEventImpl extends RaceLogEventImpl implements RaceLogStartOfTrackingEvent {

    public RaceLogStartOfTrackingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint startOfTracking, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, author, startOfTracking, pId, pInvolvedBoats, pPassId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
