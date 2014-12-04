package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public abstract class AbstractDeviceMappingEventImpl<VisitorT, ItemType extends WithID> extends AbstractLogEventImpl<VisitorT> implements
DeviceMappingEvent<VisitorT, ItemType> {
    private static final long serialVersionUID = -8439653251231710356L;

    private final ItemType mappedTo;
    private final DeviceIdentifier device;
    private final TimePoint from;
    private final TimePoint to;

    public AbstractDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, ItemType mappedTo,
            DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, id);
        this.mappedTo = mappedTo;
        this.device = device;
        this.from = from;
        this.to = to;
    }

    public ItemType getMappedTo() {
        return mappedTo;
    }
    public DeviceIdentifier getDevice() {
        return device;
    }
    public TimePoint getFrom() {
        return from;
    }
    public TimePoint getTo() {
        return to;
    }
    
    @Override
    public String getShortInfo() {
        return "device: " + device + ", mapped to: " + mappedTo + ", from: " + from + ", to: " + to;
    }
}
