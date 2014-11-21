package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.FixedMarkPassingEvent;
import com.sap.sse.common.TimePoint;


public class FixedMarkPassingEventImpl extends RaceLogEventImpl implements FixedMarkPassingEvent {

    private final Integer zeroBasedIndexOfWaypointOfPassing;
    
    private final TimePoint timePointOfPassing;

    public FixedMarkPassingEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
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


}
