package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;
import com.sap.sse.common.TimePoint;

public class RaceLogORCImpliedWindSourceEventImpl extends AbstractLogEventImpl<RaceLogEventVisitor> implements RaceLogORCImpliedWindSourceEvent {
    private static final long serialVersionUID = -792597085691551242L;
    private final RaceLogEventData raceLogEventData;
    private final ImpliedWindSource impliedWindSource;

    public RaceLogORCImpliedWindSourceEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, ImpliedWindSource impliedWindSource) {
        super(createdAt, logicalTimePoint, author, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ null, passId);
        this.impliedWindSource = impliedWindSource;
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
    public ImpliedWindSource getImpliedWindSource() {
        return impliedWindSource;
    }

    @Override
    public String getShortInfo() {
        return "Using implied wind  "+getImpliedWindSource();
    }
}
