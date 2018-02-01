package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogDeviceMarkMappingFinder extends BaseRegattaLogDeviceMappingFinder<Mark> {

    public RegattaLogDeviceMarkMappingFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceMarkMappingEvent;
    }

    @Override
    protected RegattaLogDeviceMarkMappingEvent createDeviceMappingEvent(Mark item, AbstractLogEventAuthor author,
            TimePoint from, TimePoint to, DeviceIdentifier deviceId) {
        final TimePoint now = MillisecondsTimePoint.now();
        return new RegattaLogDeviceMarkMappingEventImpl(now, now, author, UUID.randomUUID(), item, deviceId, from, to);
    }
}
