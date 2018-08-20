package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class RegattaLogDeviceMarkMappingEventImpl extends RegattaLogDeviceMappingEventImpl<Mark> implements
        RegattaLogDeviceMarkMappingEvent {
    private static final long serialVersionUID = -7223543830755457196L;

    public RegattaLogDeviceMarkMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Mark mappedTo, DeviceIdentifier device, TimePoint from,
            TimePoint toInclusive) {
        super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, toInclusive);
    }

    public RegattaLogDeviceMarkMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Mark mappedTo, DeviceIdentifier device, TimePoint from, TimePoint toInclusive) {
        super(logicalTimePoint, author, mappedTo, device, from, toInclusive);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MappingEventVisitor visitor) {
        visitor.visit(this);
    }
}
