package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogPassChangeEventImpl extends RaceLogEventImpl implements RaceLogPassChangeEvent {
    private static final long serialVersionUID = -3737606977320640630L;

    public RaceLogPassChangeEventImpl(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
