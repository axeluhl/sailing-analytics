package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Mapping event for devices mapped for Bravo extended type fixes.
 */
public class RegattaLogDeviceBoatBravoExtendedMappingEventImpl extends AbstractRegattaLogDeviceBoatSensorDataMappingEventImpl {
    private static final long serialVersionUID = -1494030544804758753L;


    public RegattaLogDeviceBoatBravoExtendedMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, to);
    }

    public RegattaLogDeviceBoatBravoExtendedMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Boat mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(logicalTimePoint, author, mappedTo, device, from, to);
    }
}
