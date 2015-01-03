package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.shared.events.impl.AbstractDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public abstract class AbstractRaceLogDeviceMappingEventImpl<ItemT extends WithID>
extends AbstractDeviceMappingEventImpl<RaceLogEventVisitor, ItemT> implements RaceLogEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    private final RaceLogEventData raceLogEventData;
    
    public AbstractRaceLogDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, int passId, ItemT mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, id, mappedTo, device, from, to);
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
}
