package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.FixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;


public class FixedMarkPassingEventImpl extends RaceLogEventImpl implements FixedMarkPassingEvent {

    private final Integer zeroBasedIndexOfWaypointOfPassing;
    
    private final TimePoint timePointOfPassing;

    public FixedMarkPassingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, TimePoint timePoint, Integer zeroBasedIndexOfWaypointOfPassing) {
        super(createdAt, author, logicalTimePoint, pId, pInvolvedBoats, pPassId);
        this.timePointOfPassing = timePoint;
        this.zeroBasedIndexOfWaypointOfPassing = zeroBasedIndexOfWaypointOfPassing;
    }

    private static final long serialVersionUID = -1796278009919318553L;

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Integer getZeroBasedIndexOfPassedWaypoint() {
        return zeroBasedIndexOfWaypointOfPassing;
    }

    @Override
    public TimePoint getTimePointOfFixedPassing() {
        return timePointOfPassing;
    }

    @Override
    public String getShortInfo() {
        return getInvolvedBoats().get(0).getName()+" at mark "+getZeroBasedIndexOfPassedWaypoint()+" at "+getTimePointOfFixedPassing();
    }
}
