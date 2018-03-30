package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogSuppressedMarkPassingsEventImpl extends RaceLogEventImpl implements
        RaceLogSuppressedMarkPassingsEvent {

    private static final long serialVersionUID = 3665678555023150888L;

    private final Integer indexOfFirstSuppressedWaypoint;

    public RaceLogSuppressedMarkPassingsEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId,
            Integer indexOfFirstSuppressedWaypoint) {
        super(createdAt, logicalTimePoint, author, pId, pInvolvedBoats, pPassId);
        this.indexOfFirstSuppressedWaypoint = indexOfFirstSuppressedWaypoint;
    }

    public RaceLogSuppressedMarkPassingsEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Competitor competitor, int pPassId, Integer indexOfFirstSuppressedWaypoint) {
        this(now(), logicalTimePoint, author, randId(), Collections.singletonList(competitor), pPassId,
                indexOfFirstSuppressedWaypoint);
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
        return getInvolvedCompetitors().get(0).getName() + " at mark " + getZeroBasedIndexOfFirstSuppressedWaypoint();
    }
}
