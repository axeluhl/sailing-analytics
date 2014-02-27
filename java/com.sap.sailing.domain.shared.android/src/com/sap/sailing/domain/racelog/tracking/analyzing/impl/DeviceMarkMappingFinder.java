package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;

public class DeviceMarkMappingFinder extends DeviceMappingFinder<Mark> {

    public DeviceMarkMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceMarkMappingEvent;
    }
}
