package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogDeviceCompetitorMappingFinder extends BaseRegattaLogDeviceMappingFinder<Competitor> {

    public RegattaLogDeviceCompetitorMappingFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceCompetitorMappingEvent;
    }

    @Override
    protected RegattaLogDeviceCompetitorMappingEvent createDeviceMappingEvent(Competitor item, AbstractLogEventAuthor author,
            TimePoint from, TimePoint toInclusive, DeviceIdentifier deviceId) {
        final TimePoint now = MillisecondsTimePoint.now();
        return new RegattaLogDeviceCompetitorMappingEventImpl(now, now, author, UUID.randomUUID(), item, deviceId, from, toInclusive);
    }
}
