package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogDeviceCompetitorBravoMappingFinder extends RegattaLogDeviceMappingFinder<Competitor> {

    public RegattaLogDeviceCompetitorBravoMappingFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        // TODO use more concrete type or interface to delimit valid mappings
        return mapping instanceof RegattaLogDeviceCompetitorSensorDataMappingEvent;
    }

    @Override
    protected RegattaLogDeviceCompetitorSensorDataMappingEvent createDeviceMappingEvent(Competitor item, AbstractLogEventAuthor author,
            TimePoint from, TimePoint to, DeviceIdentifier deviceId) {
        final TimePoint now = MillisecondsTimePoint.now();
        return new RegattaLogDeviceCompetitorBravoMappingEventImpl(now, now, author, UUID.randomUUID(), item, deviceId, from, to);
    }
}
