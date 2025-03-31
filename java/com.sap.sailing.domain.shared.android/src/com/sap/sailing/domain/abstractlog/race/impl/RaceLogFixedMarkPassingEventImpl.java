package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogFixedMarkPassingEventImpl extends RaceLogEventImpl implements RaceLogFixedMarkPassingEvent {

    private final Integer zeroBasedIndexOfWaypointOfPassing;

    private final TimePoint timePointOfPassing;

    public RaceLogFixedMarkPassingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId,
            TimePoint timePoint, Integer zeroBasedIndexOfWaypointOfPassing) {
        super(createdAt, logicalTimePoint, author, pId, pInvolvedBoats, pPassId);
        this.timePointOfPassing = timePoint;
        this.zeroBasedIndexOfWaypointOfPassing = zeroBasedIndexOfWaypointOfPassing;
    }

    public RaceLogFixedMarkPassingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Competitor competitor, int pPassId, TimePoint timePoint, Integer zeroBasedIndexOfWaypointOfPassing) {
        this(now(), logicalTimePoint, author, randId(), Collections.singletonList(competitor), pPassId, timePoint,
                zeroBasedIndexOfWaypointOfPassing);
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
        return (getInvolvedCompetitors() == null || getInvolvedCompetitors().get(0) == null ? "Unknown" :
            getInvolvedCompetitors().get(0).getName()) + " at mark " + getZeroBasedIndexOfPassedWaypoint() + " at "
                + getTimePointOfFixedPassing();
    }
}
