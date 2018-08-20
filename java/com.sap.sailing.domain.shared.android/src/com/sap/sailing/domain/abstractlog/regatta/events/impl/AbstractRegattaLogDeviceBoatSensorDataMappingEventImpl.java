package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Base type for mapping event for devices mapped for specific Bravo type fixes.
 */
public abstract class AbstractRegattaLogDeviceBoatSensorDataMappingEventImpl extends RegattaLogDeviceMappingEventImpl<Boat>
        implements RegattaLogDeviceBoatSensorDataMappingEvent {
    private static final long serialVersionUID = -1494030544804758753L;

    public AbstractRegattaLogDeviceBoatSensorDataMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, to);
    }

    public AbstractRegattaLogDeviceBoatSensorDataMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Boat mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(logicalTimePoint, author, mappedTo, device, from, to);
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
