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
    private final TimePoint closingTimePoint;

    public RegattaLogCloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
             Serializable pId, Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        super(createdAt, logicalTimePoint, author, pId);
        this.deviceMappingEventId = deviceMappingEventId;
        this.closingTimePoint = closingTimePoint;
    }

    public RegattaLogCloseOpenEndedDeviceMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,  
            Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        this( now(), author, logicalTimePoint, randId(), deviceMappingEventId, closingTimePoint);
    }

    @Override
    public Serializable getDeviceMappingEventId() {
        return deviceMappingEventId;
    }

    @Override
    public TimePoint getClosingTimePoint() {
        return closingTimePoint;
    }

    @Override
    public String getShortInfo() {
        return "closing mapping (id: " + deviceMappingEventId + ") at " + closingTimePoint;
    }
    
    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
