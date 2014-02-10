package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public interface DeviceMappingEvent<ItemType extends WithID> extends RaceLogEvent {
    ItemType getMappedTo();
    DeviceIdentifier getDevice();
    
    TimePoint getFrom();
    TimePoint getTo();
}
