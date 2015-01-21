package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
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

    public RaceLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, author, logicalTimePoint, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(pInvolvedBoats, pPassId);
    }
    
    @Override
    public List<Competitor> getInvolvedBoats() {
        return raceLogEventData.getInvolvedBoats();
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
