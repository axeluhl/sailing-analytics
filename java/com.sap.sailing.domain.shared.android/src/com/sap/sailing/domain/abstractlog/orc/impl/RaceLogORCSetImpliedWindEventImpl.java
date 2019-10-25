package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCSetImpliedWindEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class RaceLogORCSetImpliedWindEventImpl extends AbstractLogEventImpl<RaceLogEventVisitor> implements RaceLogORCSetImpliedWindEvent {
    private static final long serialVersionUID = -792597085691551242L;
    private final RaceLogEventData raceLogEventData;
    private final Speed impliedWindSpeed;

    public RaceLogORCSetImpliedWindEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, Speed impliedWindSpeed) {
        super(createdAt, logicalTimePoint, author, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ null, passId);
        this.impliedWindSpeed = impliedWindSpeed;
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
    public Speed getImpliedWindSpeed() {
        return impliedWindSpeed;
    }

    @Override
    public String getShortInfo() {
        return "Using implied wind  "+getImpliedWindSpeed();
    }
}
