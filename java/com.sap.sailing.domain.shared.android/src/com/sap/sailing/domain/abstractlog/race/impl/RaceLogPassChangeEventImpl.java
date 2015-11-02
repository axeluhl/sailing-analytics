package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogPassChangeEventImpl extends RaceLogEventImpl implements RaceLogPassChangeEvent {
    private static final long serialVersionUID = -3737606977320640630L;

    public RaceLogPassChangeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, pTimePoint, author, pId, pInvolvedBoats, pPassId);
    }

    public RaceLogPassChangeEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId) {
        super(pTimePoint, author, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
