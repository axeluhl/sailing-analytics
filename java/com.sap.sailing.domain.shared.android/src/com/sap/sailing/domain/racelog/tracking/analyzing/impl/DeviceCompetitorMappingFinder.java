package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;

public class DeviceCompetitorMappingFinder extends DeviceMappingFinder<Competitor> {

    public DeviceCompetitorMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceCompetitorMappingEvent;
    }
}
