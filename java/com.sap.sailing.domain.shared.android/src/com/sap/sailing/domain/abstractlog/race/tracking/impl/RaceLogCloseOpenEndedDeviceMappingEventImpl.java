package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogCloseOpenEndedDeviceMappingEventImpl extends BaseCloseOpenEndedDeviceMappingEventImpl<RaceLogEventVisitor>
implements RaceLogCloseOpenEndedDeviceMappingEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    private final RaceLogEventData raceLogEventData;
    
    public RaceLogCloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, int passId, Serializable deviceMappingEventId,
            TimePoint closingTimePoint) {
        super(createdAt, author, logicalTimePoint, id, deviceMappingEventId, closingTimePoint);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }
    
    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public List<Competitor> getInvolvedBoats() {
        return raceLogEventData.getInvolvedBoats();
    }

    @Override
    public String toString() {
        return raceLogEventData.toString();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
