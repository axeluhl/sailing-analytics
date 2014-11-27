package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Mark;

public class DeviceMarkMappingFinder extends DeviceMappingFinder<Mark> {

    public DeviceMarkMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceMarkMappingEvent;
    }
}
