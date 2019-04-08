package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class RegattaLogDeviceBoatMappingEventImpl extends RegattaLogDeviceMappingEventImpl<Boat>
        implements RegattaLogDeviceBoatMappingEvent {
    public RegattaLogDeviceBoatMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint toInclusive) {
        super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, toInclusive);
    }

    public RegattaLogDeviceBoatMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Boat mappedTo, DeviceIdentifier device, TimePoint from, TimePoint toInclusive) {
        super(logicalTimePoint, author, mappedTo, device, from, toInclusive);
    }

    private static final long serialVersionUID = -1494030544804758753L;

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MappingEventVisitor visitor) {
        visitor.visit(this);
    }
}
