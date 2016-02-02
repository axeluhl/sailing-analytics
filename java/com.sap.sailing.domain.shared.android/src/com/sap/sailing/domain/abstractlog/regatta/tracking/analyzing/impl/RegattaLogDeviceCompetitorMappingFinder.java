package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;

public class RegattaLogDeviceCompetitorMappingFinder extends RegattaLogDeviceMappingFinder<Competitor> {

    public RegattaLogDeviceCompetitorMappingFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceCompetitorMappingEvent;
    }
}
