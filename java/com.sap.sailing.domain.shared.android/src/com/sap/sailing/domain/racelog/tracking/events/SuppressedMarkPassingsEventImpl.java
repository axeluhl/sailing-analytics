package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.SuppressedMarkPassingsEvent;
import com.sap.sse.common.TimePoint;

public class SuppressedMarkPassingsEventImpl extends RaceLogEventImpl implements SuppressedMarkPassingsEvent {

    private static final long serialVersionUID = 3665678555023150888L;

    private final Integer indexOfFirstSuppressedWaypoint;

    public SuppressedMarkPassingsEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId,
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

}
