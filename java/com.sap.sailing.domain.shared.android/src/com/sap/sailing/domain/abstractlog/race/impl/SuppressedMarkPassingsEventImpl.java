package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.SuppressedMarkPassingsEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class SuppressedMarkPassingsEventImpl extends RaceLogEventImpl implements SuppressedMarkPassingsEvent {

    private static final long serialVersionUID = 3665678555023150888L;

    private final Integer indexOfFirstSuppressedWaypoint;

    public SuppressedMarkPassingsEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId,
            List<Competitor> pInvolvedBoats, int pPassId, Integer indexOfFirstSuppressedWaypoint) {
        super(createdAt, author, logicalTimePoint, pId, pInvolvedBoats, pPassId);
        this.indexOfFirstSuppressedWaypoint = indexOfFirstSuppressedWaypoint;

    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Integer getZeroBasedIndexOfFirstSuppressedWaypoint() {
        return indexOfFirstSuppressedWaypoint;
    }

    @Override
    public String getShortInfo() {
        return getInvolvedBoats().get(0).getName()+" at mark "+getZeroBasedIndexOfFirstSuppressedWaypoint();
    }
}
