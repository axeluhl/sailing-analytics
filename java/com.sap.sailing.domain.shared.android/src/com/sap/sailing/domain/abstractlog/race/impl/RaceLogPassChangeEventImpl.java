package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

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
