package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Mapping event for devices mapped for "Expedition" extended type fixes, coming from the
 * "Expedition" software that was originally used in the context of this solution only for
 * handling wind data but now is also used to forward sensor data into competitor tracks.
 */
public class RegattaLogDeviceBoatExpeditionExtendedMappingEventImpl extends AbstractRegattaLogDeviceBoatSensorDataMappingEventImpl {
    private static final long serialVersionUID = -7865925670153838864L;

    public RegattaLogDeviceBoatExpeditionExtendedMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, to);
    }

    public RegattaLogDeviceBoatExpeditionExtendedMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Boat mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(logicalTimePoint, author, mappedTo, device, from, to);
    }
}
