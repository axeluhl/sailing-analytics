package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sse.common.TimePoint;

public class RegattaLogCloseOpenEndedDeviceMappingEventImpl extends AbstractLogEventImpl<RegattaLogEventVisitor>
        implements RegattaLogCloseOpenEndedDeviceMappingEvent {
    private static final long serialVersionUID = -5114645637316367845L;

    private final Serializable deviceMappingEventId;
    private final TimePoint closingTimePointInclusive;

    public RegattaLogCloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
             Serializable pId, Serializable deviceMappingEventId, TimePoint closingTimePointInclusive) {
        super(createdAt, logicalTimePoint, author, pId);
        this.deviceMappingEventId = deviceMappingEventId;
        this.closingTimePointInclusive = closingTimePointInclusive;
    }

    public RegattaLogCloseOpenEndedDeviceMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,  
            Serializable deviceMappingEventId, TimePoint closingTimePointInclusive) {
        this(now(), author, logicalTimePoint, randId(), deviceMappingEventId, closingTimePointInclusive);
    }

    @Override
    public Serializable getDeviceMappingEventId() {
        return deviceMappingEventId;
    }

    @Override
    public TimePoint getClosingTimePointInclusive() {
        return closingTimePointInclusive;
    }

    @Override
    public String getShortInfo() {
        return "closing mapping (id: " + deviceMappingEventId + ") at " + closingTimePointInclusive;
    }
    
    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
