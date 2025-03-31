package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public abstract class RaceLogEventImpl extends AbstractLogEventImpl<RaceLogEventVisitor> implements RaceLogEvent {
    private static final long serialVersionUID = -2557594972618769182L;
    private final RaceLogEventData raceLogEventData;

    public RaceLogEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, logicalTimePoint, author, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(pInvolvedBoats, pPassId);
    }

    public RaceLogEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId) {
        this(createdAt, logicalTimePoint, author, pId, Collections.<Competitor>emptyList(), pPassId);
    }

    public RaceLogEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Competitor competitor, int pPassId) {
        this(now(), logicalTimePoint, author, randId(), Collections.singletonList(competitor), pPassId);
    }

    public RaceLogEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId) {
        this(now(), logicalTimePoint, author, randId(), Collections.<Competitor>emptyList(), pPassId);
    }
    
    @Override
    public <T extends Competitor> List<T> getInvolvedCompetitors() {
        return raceLogEventData.getInvolvedCompetitors();
    }
    
    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }
    
    @Override
    public String toString() {
        return raceLogEventData.toString();
    }
}
