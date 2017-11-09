package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.WithID;

public class DeviceMappingWithRegattaLogEventImpl<ItemType extends WithID> extends DeviceMappingImpl<ItemType>
        implements DeviceMappingWithRegattaLogEvent<ItemType> {
    private static final long serialVersionUID = 1495968054831506408L;
    private final RegattaLogDeviceMappingEvent<ItemType> event;
    
    public DeviceMappingWithRegattaLogEventImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange,
            RegattaLogDeviceMappingEvent<ItemType> event) {
        super(mappedTo, device, timeRange, event.getClass());
        this.event = event;
    }

    public DeviceMappingWithRegattaLogEventImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange,
            List<Serializable> originalRaceLogEventIds, RegattaLogDeviceMappingEvent<ItemType> event) {
        super(mappedTo, device, timeRange, originalRaceLogEventIds, event.getClass());
        this.event = event;
    }


    @Override
    public RegattaLogDeviceMappingEvent<ItemType> getRegattaLogEvent() {
        return event;
    }
}
