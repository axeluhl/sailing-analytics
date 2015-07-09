package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogEndOfTrackingEventImpl extends RaceLogEventImpl implements RaceLogEndOfTrackingEvent {
    private static final long serialVersionUID = 3130293807404190473L;

    public RaceLogEndOfTrackingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint endOfTracking, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, author, endOfTracking, pId, pInvolvedBoats, pPassId);

    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);

    }
}
