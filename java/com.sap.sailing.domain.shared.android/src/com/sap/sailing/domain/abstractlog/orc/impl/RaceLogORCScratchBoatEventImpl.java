package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCScratchBoatEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogORCScratchBoatEventImpl extends AbstractLogEventImpl<RaceLogEventVisitor> implements RaceLogORCScratchBoatEvent {
    private static final long serialVersionUID = 3506411337361892600L;
    private final RaceLogEventData raceLogEventData;

    public RaceLogORCScratchBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, Competitor competitor) {
        super(createdAt, logicalTimePoint, author, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ Collections.singletonList(competitor), passId);
    }
    
    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public <T extends Competitor> List<T> getInvolvedCompetitors() {
        return raceLogEventData.getInvolvedCompetitors();
    }

    @Override
    public String getShortInfo() {
        return "Setting scratch boat to competitor "+getCompetitor();
    }
}
