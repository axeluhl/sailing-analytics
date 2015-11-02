package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.CloseOpenEndedDeviceMappingEvent;
import com.sap.sse.common.TimePoint;

public abstract class BaseCloseOpenEndedDeviceMappingEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT>
        implements CloseOpenEndedDeviceMappingEvent<VisitorT> {
    private static final long serialVersionUID = -2401732623610224918L;

    private final Serializable deviceMappingEventId;
    private final TimePoint closingTimePoint;

    public BaseCloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        super(createdAt, logicalTimePoint, author, pId);
        this.deviceMappingEventId = deviceMappingEventId;
        this.closingTimePoint = closingTimePoint;
    }

    public BaseCloseOpenEndedDeviceMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        super(logicalTimePoint, author);
        this.deviceMappingEventId = deviceMappingEventId;
        this.closingTimePoint = closingTimePoint;
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
}
