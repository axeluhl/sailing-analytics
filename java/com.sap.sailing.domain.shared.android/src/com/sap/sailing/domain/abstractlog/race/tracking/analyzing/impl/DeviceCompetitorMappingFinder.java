package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;

public class DeviceCompetitorMappingFinder extends DeviceMappingFinder<Competitor> {

    public DeviceCompetitorMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceCompetitorMappingEvent;
    }
}
